const distance = (a, b) => Math.hypot(a[0] - b[0], a[1] - b[1]);

export function resample(points, count = 32, minimumLength = 8) {
  const lengths = [0];
  for (let i = 1; i < points.length; i++) lengths.push(lengths[i - 1] + distance(points[i - 1], points[i]));
  if (lengths.at(-1) < minimumLength) return null;
  let index = 1;
  return Array.from({ length: count }, (_, i) => {
    const target = lengths.at(-1) * i / (count - 1);
    while (index < points.length - 1 && lengths[index] < target) index++;
    const gap = lengths[index] - lengths[index - 1];
    const fraction = gap ? (target - lengths[index - 1]) / gap : 0;
    return [0, 1].map(j => points[index - 1][j] + fraction * (points[index][j] - points[index - 1][j]));
  });
}

function preservesBend(trace, expected) {
  const profile = path => {
    const start = path[0], end = path.at(-1);
    const dx = end[0] - start[0], dy = end[1] - start[1], chord = Math.hypot(dx, dy);
    return { chord, values: chord < 8 ? null : path.map(p => (dx * (p[1] - start[1]) - dy * (p[0] - start[0])) / chord) };
  };
  const target = profile(expected);
  if (!target.values) return true;
  const peak = target.values.reduce((best, value, i) => Math.abs(value) > Math.abs(target.values[best]) ? i : best, 0);
  const amplitude = Math.abs(target.values[peak]);
  if (amplitude < Math.max(20, target.chord * .04)) return true;
  const actual = profile(trace);
  if (!actual.values) return false;
  const sign = target.values[peak] > 0 ? 1 : -1;
  return Math.max(...actual.values.slice(Math.max(0, peak - 3), peak + 4).map(value => sign * value)) >= amplitude * .35;
}

// Stroke-level feedback. LocalStudyEngine independently checks submitted writing.
export function checkStroke(points, median, alternatives = [], tolerance = 1) {
  if (!Array.isArray(points) || points.length < 2 || points.length > 512
      || points.some(p => !Array.isArray(p) || p.length !== 2 || p.some(v => typeof v !== 'number' || !Number.isFinite(v) || v < -160 || v > 1184))) return false;
  if (!Array.isArray(median) || median.length < 2) return false;
  const trace = resample(points), expected = resample(median);
  if (!trace || !expected) return false;
  if (!preservesBend(trace, expected)) return false;
  const distances = trace.map((p, i) => distance(p, expected[i]));
  const length = p => p.slice(1).reduce((n, point, i) => n + distance(point, p[i]), 0);
  const targetLength = length(median);
  const ratio = length(points) / targetLength;
  const forwardError = distances.reduce((a, b) => a + b) / distances.length;
  const reverseError = trace.reduce((sum, point, i) => sum + distance(point, expected[expected.length - 1 - i]), 0) / trace.length;
  const directionMargin = Math.max(1, Math.min(8, targetLength * 0.05));
  const closerStroke = alternatives.some(candidate => {
    const other = resample(candidate);
    return other && trace.reduce((sum, point, i) => sum + distance(point, other[i]), 0) / trace.length + 2 < forwardError;
  });
  return distances[0] <= 105*tolerance && distances.at(-1) <= 105*tolerance
    && !closerStroke && forwardError <= 80*tolerance && forwardError + directionMargin < reverseError
    && Math.max(...distances) <= 155*tolerance && ratio >= 0.5 && ratio <= 1.8;
}

// Guided writing checks the next stroke; recall checks the whole character.
// Both use elastic path matching, not a calligraphic bend or exact endpoint.
const validPath = path => Array.isArray(path) && path.length >= 2 && path.length <= 512 &&
  path.every(p => Array.isArray(p) && p.length === 2 && p.every(v =>
    typeof v === 'number' && Number.isFinite(v) && v >= -160 && v <= 1184));
const pathLength = path => path.slice(1).reduce((sum,p,i) => sum + distance(p,path[i]),0);
const center = path => [0,1].map(axis => path.reduce((sum,p) => sum+p[axis],0)/path.length);
const mean = values => values.reduce((sum,v)=>sum+v,0)/values.length;

// Dynamic time warping allows a corner/hook at a different point along the
// stroke. Resampling also removes pointer speed and event density as criteria.
function elasticError(a,b) {
  let previous=new Float64Array(b.length+1).fill(Infinity); previous[0]=0;
  for(let i=0;i<a.length;i++) {
    const row=new Float64Array(b.length+1).fill(Infinity);
    for(let j=0;j<b.length;j++) {
      row[j+1]=distance(a[i],b[j])+Math.min(previous[j],previous[j+1],row[j]);
    }
    previous=row;
  }
  return previous[b.length]/(a.length+b.length);
}

function majorBend(path, requireTurn = false) {
  const start=path[0],end=path.at(-1),chord=distance(start,end);
  if(chord<.05)return 0;
  const a=path[Math.floor(path.length*.25)].map((v,i)=>v-start[i]);
  const b=end.map((v,i)=>v-path[Math.floor(path.length*.75)][i]);
  if(requireTurn && (a[0]*b[0]+a[1]*b[1])/Math.hypot(...a)/Math.hypot(...b)>.25)return 0;
  return path.reduce((peak,p)=>{
    const offset=((end[0]-start[0])*(p[1]-start[1])-(end[1]-start[1])*(p[0]-start[0]))/chord;
    return Math.abs(offset)>Math.abs(peak)?offset:peak;
  },0);
}

function strokeFit(points,median) {
  const trace=resample(points),target=resample(median);
  if(!trace || !target)return null;
  const length=pathLength(trace),expected=pathLength(target),ratio=length/expected;
  const a=center(trace),b=center(target);
  // Compare local shape independently of length; retain position separately.
  const shape=trace.map(p=>p.map((v,axis)=>(v-a[axis])/length));
  const reference=target.map(p=>p.map((v,axis)=>(v-b[axis])/expected));
  const forward=elasticError(shape,reference),reverse=elasticError(shape,[...reference].reverse());
  // Classify the reference corner only. Applying the same binary angle cutoff
  // to the handwriting creates a discontinuity: a 1-degree wobble can turn
  // an otherwise identical corner into zero curvature (自 / 設 / 兒).
  const bend=majorBend(reference,true),actualBend=majorBend(shape);
  return {shape:forward,position:distance(a,b),ratio,actualCenter:a,targetCenter:b,
    direction:forward+.008<reverse,
    // A brush entry less than 1/32 of the reference grid is a font detail.
    // Require both substantial depth and substantial relative curvature.
    structure:Math.abs(bend)<Math.max(.16,32/expected) || actualBend*Math.sign(bend)>Math.abs(bend)*.22};
}

export function checkLessonStroke(points, median, alternatives = [], tolerance = 1) {
  if(!validPath(points)||!validPath(median))return false;
  const fit=strokeFit(points,median);
  if(!fit || !fit.direction || !fit.structure || fit.ratio<.30 || fit.ratio>2.70 ||
      fit.shape>.13*tolerance || fit.position>105*tolerance)return false;
  // Keep guided stroke-order instruction. A nearby parallel stroke cannot
  // substitute for the requested stroke just because both are horizontal.
  return !alternatives.some(other=>{
    if(other===median || !validPath(other))return false;
    const alternative=strokeFit(points,other);
    return alternative && alternative.direction && alternative.shape<.08 &&
      alternative.position+35<fit.position && alternative.shape<=fit.shape+.015;
  });
}

function writingBounds(paths) {
  let x0=Infinity,y0=Infinity,x1=-Infinity,y1=-Infinity;
  for(const path of paths)for(const [x,y] of path) {
    x0=Math.min(x0,x);x1=Math.max(x1,x);y0=Math.min(y0,y);y1=Math.max(y1,y);
  }
  return {center:[(x0+x1)/2,(y0+y1)/2],width:x1-x0,height:y1-y0,size:Math.max(x1-x0,y1-y0)};
}

// Shape evidence is averaged over the whole character. A small hook/serif
// mismatch no longer vetoes all twelve strokes of 萬. Major missing/broken
// strokes, direction, order and distinguishing proportions remain checked.
export function checkLessonWriting(strokes, medians) {
  if(!Array.isArray(medians)||!medians.length||medians.length>64 ||
      !Array.isArray(strokes)||strokes.length!==medians.length ||
      !strokes.every(validPath)||!medians.every(validPath))return false;
  const drawn=writingBounds(strokes),target=writingBounds(medians),ratio=drawn.size/target.size;
  // Recall is about shape, not how much of the canvas was filled. Normalize
  // before stroke sampling so small but deliberate strokes remain readable.
  if(!Number.isFinite(ratio)||ratio<=0||drawn.size<24)return false;
  const aspect=(drawn.width/drawn.height)/(target.width/target.height);
  if(target.width>target.size*.25 && target.height>target.size*.25 &&
      (!Number.isFinite(aspect)||aspect<.60||aspect>1.65))return false;
  // Allow wider/narrower handwriting, without stretching a horizontal line
  // into a vertical one or independently moving the character's components.
  const scale=[target.width/drawn.width,target.height/drawn.height].map((value,axis)=>
    (axis===0?target.width:target.height)>target.size*.25 && Number.isFinite(value)
      ? Math.max(.75/ratio,Math.min(1.33/ratio,value)) : 1/ratio);
  const aligned=strokes.map(path=>path.map(p=>p.map((v,a)=>(v-drawn.center[a])*scale[a]+target.center[a])));
  const fits=aligned.map((path,i)=>strokeFit(path,medians[i]));
  if(fits.some(f=>!f || !f.direction || !f.structure || f.ratio<.28 || f.ratio>2.8 || f.shape>.24 || f.position>target.size*.25))return false;
  if(mean(fits.map(f=>f.shape))>.105 || mean(fits.map(f=>f.position))/target.size>.095)return false;
  // Only a clearly better position AND shape identifies the wrong next stroke.
  // Do not compare unrelated long/short strokes across distant radicals.
  for(let i=0;i<fits.length;i++)for(let j=i+1;j<fits.length;j++) {
    const gap=distance(fits[i].targetCenter,fits[j].targetCenter);
    if(gap>target.size*.08 && distance(fits[i].actualCenter,fits[j].actualCenter)<gap*.20)return false;
    const swapped=distance(fits[i].actualCenter,fits[j].targetCenter)+distance(fits[j].actualCenter,fits[i].targetCenter);
    if(swapped+target.size*.08>=fits[i].position+fits[j].position)continue;
    const a=strokeFit(aligned[i],medians[j]),b=strokeFit(aligned[j],medians[i]);
    if(a && b && a.direction && b.direction &&
        a.shape+b.shape<=fits[i].shape+fits[j].shape+.02 &&
        a.position+b.position+target.size*.08<fits[i].position+fits[j].position)return false;
  }
  // A reversal of the defining horizontal lengths distinguishes 土 from 士.
  // The old all-pairs rule also compared unrelated lines in complex glyphs.
  if(medians.length<=5) {
    const vector=path=>[path.at(-1)[0]-path[0][0],path.at(-1)[1]-path[0][1]];
    for(let i=0;i<medians.length;i++)for(let j=i+1;j<medians.length;j++) {
      const a=vector(medians[i]),b=vector(medians[j]),la=Math.hypot(...a),lb=Math.hypot(...b);
      if(la<40||lb<40||Math.abs(a[1])>la*.25||Math.abs(b[1])>lb*.25 ||
          pathLength(medians[i])>la*1.15||pathLength(medians[j])>lb*1.15)continue;
      const expected=la/lb,actual=pathLength(aligned[i])/pathLength(aligned[j]);
      if((expected>1.30&&actual<.90)||(expected<1/1.30&&actual>1/.90))return false;
    }
    // Preserve a prominent endpoint crossing a horizontal bar (天 / 夫).
    // Tiny touches/serifs have a dead band and are not structural crossings.
    for(let i=0;i<medians.length;i++) {
      const bar=medians[i],v=vector(bar),length=Math.hypot(...v);
      if(length<target.size*.30||Math.abs(v[1])>length*.25||pathLength(bar)>length*1.15)continue;
      const side=(point,path)=>point[1]-(path[0][1]+(point[0]-path[0][0])*
        (path.at(-1)[1]-path[0][1])/(path.at(-1)[0]-path[0][0]));
      for(let j=0;j<medians.length;j++)if(i!==j) {
        const stroke=medians[j];
        if(Math.abs(vector(stroke)[1])<target.size*.35)continue;
        for(const end of [0,stroke.length-1]) {
          const point=stroke[end];
          if(point[0]<Math.min(bar[0][0],bar.at(-1)[0])||point[0]>Math.max(bar[0][0],bar.at(-1)[0]))continue;
          const expected=side(point,bar),actual=side(end===0?aligned[j][0]:aligned[j].at(-1),aligned[i]);
          if(Math.abs(expected)>target.size*.04 && Math.abs(actual)>target.size*.08 && expected*actual<0)return false;
        }
      }
    }
  }
  return true;
}

// Exam and lesson recall must recognize the same handwriting. The caller
// separately enforces unaided recall; geometry must not credit 55% of a glyph.
export function checkFreeWriting(strokes, medians) {
  return checkLessonWriting(strokes,medians);
}

// completed is the number of saved repetitions, not the displayed attempt number.
// This is presentation policy only; it never changes memory or assessment gates.
export function lessonWritingGuide(completed = 0) {
  const count = Number.isFinite(completed) ? Math.max(0, Math.floor(completed)) : 0;
  return count < 3 ? 'full' : count < 6 ? 'stroke' : count < 9 ? 'start' : 'none';
}

export class WritingPad {
  constructor(canvas, data, { mode = 'trace', guidance = 'full', onStroke, onComplete, onInteraction, onAttempt, label, validation = 'strict' }) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.data = data;
    this.mode = mode;
    this.validation = validation;
    this.guidance = mode === 'free' ? 'none' : guidance;
    this.assisted = mode === 'trace';
    this.onStroke = onStroke;
    this.onComplete = onComplete;
    this.onInteraction = onInteraction;
    this.onAttempt = onAttempt;
    this.label = label;
    this.accepted = [];
    this.points = [];
    this.pointer = null;
    this.demo = null;
    this.generation = 0;
    this.busy = false;
    canvas.setAttribute('aria-busy', 'false');
    this.disposed = false;
    this.abort = new AbortController();
    canvas.addEventListener('pointerdown', event => this.down(event), { signal: this.abort.signal });
    canvas.addEventListener('pointermove', event => this.move(event), { signal: this.abort.signal });
    canvas.addEventListener('pointerup', event => this.up(event), { signal: this.abort.signal });
    canvas.addEventListener('pointercancel', event => this.cancel(event), { signal: this.abort.signal });
    canvas.addEventListener('lostpointercapture', event => this.cancel(event), { signal: this.abort.signal });
    this.observer = new ResizeObserver(() => this.resize());
    this.observer.observe(canvas);
    this.resize();
    this.report(false);
  }

  resize() {
    const rect = this.canvas.getBoundingClientRect();
    const dpr = Math.min(devicePixelRatio || 1, 3);
    this.canvas.width = Math.round(rect.width * dpr);
    this.canvas.height = this.canvas.width;
    this.draw();
  }

  point(event) {
    const rect = this.canvas.getBoundingClientRect();
    return [(event.clientX - rect.left) * 1024 / rect.width, 900 - (event.clientY - rect.top) * 1024 / rect.height];
  }

  down(event) {
    if (this.disposed || this.busy || this.demo || this.pointer !== null || event.isPrimary === false
        || (event.pointerType === 'mouse' && event.button !== 0) || this.accepted.length >= this.data.medians.length) return;
    event.preventDefault();
    this.pointer = event.pointerId;
    this.canvas.setPointerCapture(event.pointerId);
    this.points = [this.point(event)];
    this.failed = false;
    this.onInteraction?.();
    this.draw();
  }

  move(event) {
    if (event.pointerId !== this.pointer) return;
    event.preventDefault();
    const samples = event.getCoalescedEvents?.() || [];
    for (const sample of samples.length ? samples : [event]) {
      const point = this.point(sample);
      if (distance(point, this.points.at(-1)) > (this.mode==='free'?.25:1.2)) this.points.push(point);
    }
    if (this.points.length > 1000) this.points = resample(this.points, 500);
    this.onInteraction?.();
    this.draw();
  }

  async up(event) {
    if (event.pointerId !== this.pointer) return;
    this.points.push(this.point(event));
    const pointer = this.pointer;
    this.pointer = null;
    if (this.canvas.hasPointerCapture(pointer)) this.canvas.releasePointerCapture(pointer);
    if (this.points.length > 512) this.points = resample(this.points, 512);
    if (!this.points || !resample(this.points,32,this.mode==='free'?.5:8)) {
      this.points=[]; this.report(true); this.draw(); return;
    }
    this.onAttempt?.(structuredClone(this.points));
    if(this.mode==='free') {
      if(this.accepted.length<this.data.medians.length)this.accepted.push(this.points);
      this.points=[];this.report(false);this.draw();return;
    }
    const validator = this.validation === 'lesson' ? checkLessonStroke : checkStroke;
    const valid = validator(this.points, this.data.medians[this.accepted.length], this.data.medians);
    if (valid) {
      this.accepted.push(this.points);
      this.points = [];
      this.failed = false;
    } else { this.failed = true; this.assisted = true; }
    this.report(!valid);
    this.draw();
    if (valid && this.accepted.length === this.data.medians.length) {
      this.busy = true;
      this.canvas.setAttribute('aria-busy', 'true');
      await this.onComplete?.(structuredClone(this.accepted), this.mode, {unassisted:this.mode==='free' && !this.assisted});
      if (!this.disposed) this.draw();
    }
  }

  cancel(event) {
    if (event.pointerId !== this.pointer) return;
    this.pointer = null;
    this.points = [];
    this.failed = false;
    this.draw();
  }

  report(error) {
    const current = Math.min(this.accepted.length + 1, this.data.medians.length);
    this.canvas.setAttribute('aria-label', this.mode==='free'?`혼자 쓰기 · 입력 ${this.accepted.length}획`:this.label(current, this.data.medians.length));
    this.onStroke?.({ current, total: this.data.medians.length, error, complete: this.mode==='trace' && this.accepted.length === this.data.medians.length,drawn:this.accepted.length });
  }

  async submitRecall() {
    if(this.busy || this.pointer!==null || !this.accepted.length || this.mode!=='free')return;
    this.busy=true;this.canvas.setAttribute('aria-busy','true');
    try {
      await this.onComplete?.(structuredClone(this.accepted),this.mode,{unassisted:!this.assisted});
    } catch(error) {
      this.busy=false;
      if(!this.disposed)this.canvas.setAttribute('aria-busy','false');
      throw error;
    }
    if(!this.disposed)this.draw();
  }

  reset({preserveAssistance=false, newAttempt=false}={}) {
    this.stopDemo();
    const pointer=this.pointer; this.pointer=null;
    if (pointer !== null && this.canvas.hasPointerCapture(pointer)) this.canvas.releasePointerCapture(pointer);
    this.accepted = [];
    this.assisted = this.mode === 'trace' || (!newAttempt && (this.assisted || preserveAssistance));
    this.points = [];
    this.failed = false;
    this.busy = false;
    this.canvas.setAttribute('aria-busy', 'false');
    this.report(false);
    this.draw();
  }

  setMode(mode) {
    if(mode===this.mode)return;
    this.mode = mode;
    if (mode === 'free') this.guidance = 'none';
    // A new mode starts a full-character attempt; partial tracing is not recall.
    this.reset({preserveAssistance:true});
  }

  // Call only after the previous repetition has been committed to storage.
  beginAttempt(mode, guidance) {
    this.mode=mode; this.guidance=mode === 'free' ? 'none' : guidance;
    this.reset({newAttempt:true});
  }

  unlock() {
    this.busy=false; this.canvas.setAttribute('aria-busy','false');
    this.report(false); this.draw();
  }

  undo() {
    if (this.busy || this.demo || this.pointer !== null || this.mode !== 'free') return;
    this.accepted.pop(); this.points=[]; this.failed=false;
    this.report(false); this.draw();
  }

  line(points, color, width = 24, dashed = false) {
    if (!points.length) return;
    const ctx = this.ctx;
    ctx.beginPath(); ctx.lineCap = 'round'; ctx.lineJoin = 'round';
    ctx.lineWidth = width; ctx.strokeStyle = color; ctx.setLineDash(dashed ? [10, 14] : []);
    points.forEach((p, i) => i ? ctx.lineTo(p[0], 900 - p[1]) : ctx.moveTo(p[0], 900 - p[1]));
    ctx.stroke(); ctx.setLineDash([]);
  }

  draw() {
    if (this.disposed || !this.ctx) return;
    const ctx = this.ctx;
    ctx.setTransform(this.canvas.width / 1024, 0, 0, this.canvas.height / 1024, 0, 0);
    ctx.clearRect(0, 0, 1024, 1024);
    if(this.mode!=='free') {
    ctx.strokeStyle = '#dddac5'; ctx.lineWidth = 2; ctx.setLineDash([12, 13]);
    ctx.beginPath(); ctx.moveTo(512, 0); ctx.lineTo(512, 1024); ctx.moveTo(0, 512); ctx.lineTo(1024, 512); ctx.stroke();
    ctx.strokeStyle = '#eeecdd'; ctx.beginPath(); ctx.moveTo(0, 0); ctx.lineTo(1024, 1024); ctx.moveTo(1024, 0); ctx.lineTo(0, 1024); ctx.stroke(); ctx.setLineDash([]);
    }
    ctx.save(); ctx.translate(0, 900); ctx.scale(1, -1);
    const centerline = this.data.source?.strokeRendering === 'centerline';
    const shape = (path, color) => {
      if (centerline) {
        ctx.strokeStyle = color; ctx.lineWidth = 25;
        ctx.lineCap = 'round'; ctx.lineJoin = 'round';
        ctx.stroke(new Path2D(path));
      } else {
        ctx.fillStyle = color; ctx.fill(new Path2D(path));
      }
    };
    if ((this.mode === 'trace' && this.guidance==='full') || this.demo) {
      for (const path of this.data.strokes) shape(path, centerline ? '#e1dfd0' : '#e9e7d8');
    }
    if(this.mode==='trace' && this.guidance==='full')this.accepted.forEach((_, i) => shape(this.data.strokes[i], '#a9c198'));
    ctx.restore();
    for (const stroke of this.accepted) this.line(stroke, '#447249', 19);
    if (!this.demo && !this.busy && this.accepted.length < this.data.medians.length) {
      const median = this.data.medians[this.accepted.length];
      if (this.mode === 'trace') {
        // Reduced guidance must not silently expand after a bad stroke.
        // The existing hint/relearn buttons provide explicit recovery.
        const showPath = this.guidance==='full' || this.guidance==='stroke';
        if(showPath)this.line(median, '#87a773', 7, true);
        const first = median[0], last = median.at(-1), before = median.at(-2);
        ctx.fillStyle = '#66894f'; ctx.beginPath(); ctx.arc(first[0], 900 - first[1], 26, 0, Math.PI * 2); ctx.fill();
        ctx.fillStyle = '#fffdf2'; ctx.font = 'bold 29px sans-serif'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle'; ctx.fillText(String(this.accepted.length + 1), first[0], 900 - first[1] + 1);
        const angle = Math.atan2(-(last[1] - before[1]), last[0] - before[0]);
        if(showPath) {
        ctx.save(); ctx.translate(last[0], 900 - last[1]); ctx.rotate(angle); ctx.fillStyle = '#66894f';
        ctx.beginPath(); ctx.moveTo(12, 0); ctx.lineTo(-15, -13); ctx.lineTo(-15, 13); ctx.closePath(); ctx.fill(); ctx.restore();
        }
      }
    }
    if (this.points.length) this.line(this.points, this.failed ? '#cf886a' : '#416b46', 23);
    if (this.demo) {
      const median = this.data.medians[this.demo.index];
      this.line((resample(median, 90) || median).slice(0, Math.max(2, Math.ceil(this.demo.fraction * 90))), '#699ba0', 27);
    }
  }

  async animate(all = true) {
    if (this.disposed || this.busy || this.pointer !== null) return;
    this.assisted = true;
    this.stopDemo();
    const generation = ++this.generation;
    const startIndex = all ? 0 : Math.min(this.accepted.length, this.data.medians.length - 1);
    const endIndex = all ? this.data.medians.length : startIndex + 1;
    for (let index = startIndex; index < endIndex; index++) {
      const began = performance.now();
      await new Promise(resolve => {
        const frame = now => {
          if (this.disposed || generation !== this.generation) { resolve(); return; }
          const fraction = Math.min(1, (now - began) / 850);
          this.demo = { index, fraction }; this.draw();
          this.onStroke?.({ current: index + 1, total: this.data.medians.length, demo: true });
          if (fraction < 1) requestAnimationFrame(frame); else resolve();
        };
        requestAnimationFrame(frame);
      });
      if (this.disposed || generation !== this.generation) return;
    }
    this.demo = null;
    this.report(false);
    this.draw();
  }

  stopDemo() {
    this.generation++;
    this.demo = null;
    if (!this.disposed) { this.report(false); this.draw(); }
  }

  destroy() {
    this.disposed = true;
    this.generation++;
    const pointer=this.pointer; this.pointer=null;
    if (pointer !== null && this.canvas.hasPointerCapture(pointer)) this.canvas.releasePointerCapture(pointer);
    this.abort.abort();
    this.observer.disconnect();
  }
}
