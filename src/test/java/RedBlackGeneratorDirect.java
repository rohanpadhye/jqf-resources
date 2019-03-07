
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.Comparator;

import static org.junit.Assume.assumeTrue;

public class RedBlackGeneratorDirect extends Generator<RedBlackTree> {
    public RedBlackGeneratorDirect() { super(RedBlackTree.class); }

    public final int K = 100;
    public final int N = 100;

    public boolean isValidRedBlackTree(RedBlackTree tree) {

        return false;
    }

    // Generates a single RedBlackTree
    private RedBlackTree generateAux(SourceOfRandomness random, GenerationStatus __ignore__, RedBlackTree tree, int SZ) {

    }

    @Override
    public RedBlackTree generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        int sz = random.nextInt(0,N );
        RedBlackTree tree = new RedBlackTree(Comparator.naturalOrder());
        //write code to generate a rbtree by populating data, color randomly and by calling generate
        // for left and right subtrees with some probability.
        generateAux(random, __ignore__, tree, sz);
        assumeTrue(isValidRedBlackTree(tree));
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
