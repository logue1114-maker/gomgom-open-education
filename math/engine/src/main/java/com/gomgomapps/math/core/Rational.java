package com.gomgomapps.math.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/** Exact arithmetic: an OCR or rounding error must never become a correct answer. */
public final class Rational implements Comparable<Rational>, Serializable {
    private static final long serialVersionUID = 1L;
    public static final Rational ZERO = of(0), ONE = of(1);
    public final BigInteger n, d;
    public Rational(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() == 0) throw new IllegalArgumentException("0으로 나눌 수 없음");
        if (numerator.bitLength() > 512 || denominator.bitLength() > 512) throw new IllegalArgumentException("수가 너무 큼");
        BigInteger gcd = numerator.gcd(denominator);
        if (denominator.signum() < 0) gcd = gcd.negate();
        n = numerator.divide(gcd); d = denominator.divide(gcd);
    }
    public static Rational of(long n) { return new Rational(BigInteger.valueOf(n), BigInteger.ONE); }
    public static Rational of(long n, long d) { return new Rational(BigInteger.valueOf(n), BigInteger.valueOf(d)); }
    public static Rational decimal(String s) {
        BigDecimal v = new BigDecimal(s);
        if (v.scale() < 0 || v.scale() > 12) throw new IllegalArgumentException("소수 자릿수 확인 필요");
        return new Rational(v.unscaledValue(), BigInteger.TEN.pow(v.scale()));
    }
    public Rational add(Rational v) { return new Rational(n.multiply(v.d).add(v.n.multiply(d)), d.multiply(v.d)); }
    public Rational sub(Rational v) { return add(v.neg()); }
    public Rational mul(Rational v) { return new Rational(n.multiply(v.n), d.multiply(v.d)); }
    public Rational div(Rational v) { return new Rational(n.multiply(v.d), d.multiply(v.n)); }
    public Rational neg() { return new Rational(n.negate(), d); }
    public Rational pow(int p) {
        if (Math.abs(p) > 12) throw new IllegalArgumentException("지수 범위 확인 필요");
        if (p < 0) return new Rational(d.pow(-p), n.pow(-p));
        if (p == 0 && n.signum() == 0) throw new IllegalArgumentException("0의 0제곱 확인 필요");
        return new Rational(n.pow(p), d.pow(p));
    }
    public boolean isZero() { return n.signum() == 0; }
    public boolean isInteger() { return d.equals(BigInteger.ONE); }
    public int intValue() { if (!isInteger()) throw new IllegalArgumentException("정수 필요"); if(n.bitLength()>31)throw new ArithmeticException("BigInteger out of int range"); return n.intValue(); }
    public Rational sqrt() {
        if (n.signum() < 0) throw new IllegalArgumentException("실수 제곱근 확인 필요");
        BigInteger a = root(n), b = root(d);
        if (!a.multiply(a).equals(n) || !b.multiply(b).equals(d)) throw new IllegalArgumentException("이 입력은 정확한 유리수로 읽을 수 없음");
        return new Rational(a,b);
    }
    public Rational cbrt() {
        BigInteger a=cubeRoot(n.abs()),b=cubeRoot(d);
        if(!a.pow(3).equals(n.abs())||!b.pow(3).equals(d))throw new IllegalArgumentException("이 입력은 정확한 유리수로 읽을 수 없음");
        return new Rational(n.signum()<0?a.negate():a,b);
    }
    private static BigInteger cubeRoot(BigInteger v){
        BigInteger lo=BigInteger.ZERO,hi=BigInteger.ONE.shiftLeft((v.bitLength()+2)/3);
        while(hi.subtract(lo).compareTo(BigInteger.ONE)>0){BigInteger mid=lo.add(hi).shiftRight(1);if(mid.pow(3).compareTo(v)<=0)lo=mid;else hi=mid;}
        return lo;
    }
    private static BigInteger root(BigInteger v) {
        BigInteger lo = BigInteger.ZERO, hi = BigInteger.ONE.shiftLeft((v.bitLength()+2)/2);
        while (hi.subtract(lo).compareTo(BigInteger.ONE)>0) { BigInteger mid=lo.add(hi).shiftRight(1); if(mid.multiply(mid).compareTo(v)<=0) lo=mid; else hi=mid; }
        return lo;
    }
    @Override public int compareTo(Rational v) { return n.multiply(v.d).compareTo(v.n.multiply(d)); }
    @Override public boolean equals(Object o) { return o instanceof Rational r && n.equals(r.n) && d.equals(r.d); }
    @Override public int hashCode() { return 31*n.hashCode()+d.hashCode(); }
    @Override public String toString() { return isInteger()?n.toString():n+"/"+d; }
    public String decimalText() {
        try { return new BigDecimal(n).divide(new BigDecimal(d)).stripTrailingZeros().toPlainString(); }
        catch (ArithmeticException e) { return toString(); }
    }
}
