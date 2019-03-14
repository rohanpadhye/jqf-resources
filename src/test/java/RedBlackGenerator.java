
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.Comparator;

public class RedBlackGenerator extends Generator<RedBlackTree> {
    public RedBlackGenerator() { super(RedBlackTree.class); }

    public final int K = 100;
    public final int N = 100;

    // Generates a single RedBlackTree
    @Override
    public RedBlackTree generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        RedBlackTree tree = new RedBlackTree(Comparator.naturalOrder());
        int num_rounds = random.nextInt(N);
        for (int i = 0; i < num_rounds; i++) {
            if (random.nextBoolean()) {
                tree.add(random.nextInt(-K, K));
            } else {
                tree.remove(random.nextInt(-K, K));
            }
        }
        return tree;
    }


}
