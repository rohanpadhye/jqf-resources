
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


    // Generates a single RedBlackTree
//    @Override
//    public RedBlackTree generate(SourceOfRandomness random, GenerationStatus __ignore__) {
//        RedBlackTree tree = new RedBlackTree(Comparator.naturalOrder());
//        int num_rounds = random.nextInt(5, 20);
//        int range_top = random.nextInt();
//        for (int i = 0; i < num_rounds; i++) {
//            if (random.nextBoolean()) {
//                // add elements
//                int num_to_add = random.nextInt(6, 15);
//                for (int j = 0; j < num_to_add; j++) {
//                    tree.add(random.nextInt(range_top));
//                }
//            } else {
//                // remove elements
//                int num_to_remove = random.nextInt(1, 6);
//                for (int j = 0; j < num_to_remove; j++) {
//                    tree.remove(random.nextInt(range_top));
//                }
//            }
//        }
//        return tree;
//    }

    //TODO
    //There need to be two generate methods
    // 1. Generating trees by calling add,remove on a red black tree randomly
    // 2. Generating trees by creating a parameterized version that can be translated into a RB tree
    // 1 seems easier so I'll do that first
}
