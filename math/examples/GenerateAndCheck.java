import com.gomgomapps.math.core.Checker;
import com.gomgomapps.math.core.Generator;
import com.gomgomapps.math.core.Question;
import java.util.List;
import java.util.Random;

/** Library usage example, not an application or learning-flow test. */
public final class GenerateAndCheck {
    public static void main(String[] args) {
        Generator generator = new Generator(new Random(42));
        Question question = generator.next("add100", List.of(), false);
        System.out.println(question.prompt);
        Checker.Result result = new Checker().check(question, List.of(), List.of("-1"));
        System.out.println("Submitted answer: -1; result: " + result.status);
    }
}
