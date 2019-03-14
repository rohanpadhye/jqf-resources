
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
    public final double leafProbability = 0.1;
    public boolean valid = true;

    public boolean isValidRedBlackTree(RedBlackTree tree) {
        // Check if it is a valid binary search tree.
        if (!isValidBST(tree)) {
            return false;
        }

        // Check if all paths through the tree have the same number of black nodes.
        if (!pathLengthVerification(tree)) {
            return false;
        }

        // Check if all red nodes have a black parent.
        if (!redNodeBlackParent(tree)) {
            return false;
        }

        return true;
    }

    public boolean isValidBST(BinarySearchTree tree) {
        BinaryTreeNode.Visitor v = new BinaryTreeNode.Visitor () {
            Object last = null;
            @Override
            public <E> void visit(BinaryTreeNode<E> node) {
                if (last == null) {
                    return;
                }
                if (tree.compare(last, node) != 1) {
                    valid = false;
                }
            }
        };
        tree.root.traverseInorder(v);
        return valid;
    }

    public boolean pathLengthVerification(RedBlackTree tree) {
        Stack<Pair<RedBlackTree.Node, Integer>> depths = new Stack<>();
        depths.push(new Pair<>((RedBlackTree.Node) tree.getRoot(), 0));
        int max_d = -1;
        while (depths.size() != 0) {
            Pair<RedBlackTree.Node, Integer> top = depths.peek();
            depths.pop();
            RedBlackTree.Node n = top.getKey();
            int d = top.getValue();
            if (n.getLeft() == null && n.getRight() == null) {
                if (max_d == -1) {
                    max_d = d;
                } else {
                    if (d != max_d) {
                        return false;
                    }
                }
            }
            if (n.isRed) {
                d = d + 1;
            }
            if (n.getLeft() != null) {
                depths.push(new Pair<>((RedBlackTree.Node) n.left, d));
            }
            if (n.getRight() != null) {
                depths.push(new Pair<>((RedBlackTree.Node) n.right, d));
            }
        }
        return true;
    }

    public boolean redNodeBlackParent(RedBlackTree tree) {
        BinaryTreeNode.Visitor v = new BinaryTreeNode.Visitor() {
            @Override
            public <E> void visit(BinaryTreeNode<E> node) {
                if (node.getLeft() == null && node.getRight() == null) {
                    return;
                }
                if (node.getParent() != null) {
                    if (((RedBlackTree.Node) node).isRed && ((RedBlackTree.Node) node.getParent()).isRed) {
                        valid = false;
                    }
                } else if (((RedBlackTree.Node) node).isRed) {
                    valid = false;
                }
            }
        };
        tree.root.traversePreorder(v);
        return valid;
    }

    // Generates a single RedBlackTree
    private RedBlackTree.Node generateAux(SourceOfRandomness random, RedBlackTree tree, int SZ) {
        int datum = random.nextInt(-K, K);
        RedBlackTree.Node node = tree.new Node(datum);
        node.isRed = random.nextBoolean();
        if (random.nextDouble() >= leafProbability) {
            node.left = generateAux(random, tree, SZ);
            node.right = generateAux(random, tree, SZ);
        }
        return node;
    }

    // Generates a single RedBlackTree
    private RedBlackTree.Node generateIntervalAux(SourceOfRandomness random, RedBlackTree tree, int maxDepth, int min, int max) {
        int datum = random.nextInt(min, max);
        RedBlackTree.Node node = tree.new Node(datum);
        node.isRed = random.nextBoolean();

        if (random.nextDouble() >= leafProbability && maxDepth >= 0) {
            if (random.nextDouble() >= leafProbability) {
                node.setLeft(generateIntervalAux(random, tree, maxDepth-1, min, datum));
                node.setRight(generateIntervalAux(random, tree, maxDepth-1, datum, max));
            } else {
                if (random.nextBoolean()) {
                    node.setLeft(generateIntervalAux(random, tree, maxDepth - 1, min, datum));
                } else {
                    node.setRight(generateIntervalAux(random, tree, maxDepth - 1, datum, max));
                }
            }
        }
        return node;
    }

    @Override
    public RedBlackTree generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        int treeDepth = random.nextInt(0,N);
        RedBlackTree tree = new RedBlackTree(Comparator.naturalOrder());
        tree.setRoot(generateIntervalAux(random, tree, treeDepth, -K, K));
        assumeTrue(isValidRedBlackTree(tree));
        return tree;
    }

}
