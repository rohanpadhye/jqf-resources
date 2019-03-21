
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import javafx.util.Pair;

import javax.imageio.metadata.IIOInvalidTreeException;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import static org.junit.Assume.assumeTrue;

public class RedBlackGeneratorDirect extends Generator<RedBlackTree> {
    public RedBlackGeneratorDirect() { super(RedBlackTree.class); }

    public final int K = 100;
    public final int N = 100;

    // Generates a single RedBlackTree
    private RedBlackTree.Node generateAux(SourceOfRandomness random, RedBlackTree tree, int SZ) {
        int datum = random.nextInt(-K, K);
        RedBlackTree.Node node = tree.new Node(datum);
        node.isRed = random.nextBoolean();
        if (random.nextBoolean()) {
            node.left = generateAux(random, tree, SZ);
            node.right = generateAux(random, tree, SZ);
        }
        return node;
    }

    // Generates a single RedBlackTree
    private RedBlackTree.Node generateIntervalAux(SourceOfRandomness random, RedBlackTree tree, int maxDepth, int min, int max) {
        int data = random.nextInt(min, max);
        RedBlackTree.Node node = tree.new Node(data);
        node.isRed = random.nextBoolean();

        if (random.nextBoolean() && maxDepth >= 0) {
            if (random.nextBoolean()) {
                node.setLeft(generateIntervalAux(random, tree, maxDepth-1, min, data));
                node.setRight(generateIntervalAux(random, tree, maxDepth-1, data, max));
            } else {
                if (random.nextBoolean()) {
                    node.setLeft(generateIntervalAux(random, tree, maxDepth - 1, min, data));
                } else {
                    node.setRight(generateIntervalAux(random, tree, maxDepth - 1, data, max));
                }
            }
        }
        return node;
    }

    @Override
    public RedBlackTree generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        int treeDepth = random.nextInt(0,100);
        RedBlackTree tree = new RedBlackTree(Comparator.naturalOrder());
        tree.setRoot(generateIntervalAux(random, tree, 1, -K, K));
        return tree;
    }

}
