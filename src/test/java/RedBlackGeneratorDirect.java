
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
        BinaryTreeNode root = tree.getRoot();
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
//        boolean valid = true;
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

    public int pathLength(RedBlackTree tree, RedBlackTree.Node node) {
        // if root
        if (node == tree.getRoot()) {
            return 1;
        }
        assert(node.getParent() != null);
        if (!node.isRed) {
            return 1 + pathLength(tree, (RedBlackTree.Node) node.getParent());
        } else {
            return pathLength(tree, (RedBlackTree.Node) node.getParent());
        }
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
    private RedBlackTree.Node generateIntervalAux(SourceOfRandomness random, RedBlackTree tree, int depth, int min, int max) {
//            node.left;
        int datum = random.nextInt(min, max);
        RedBlackTree.Node node = tree.new Node(datum);
        if (node.getParent() != null) {
            if (((RedBlackTree.Node) node.getParent()).isRed) {
                node.isRed = false;
            }
//        node.isRed = random.nextBoolean();
        } else {
            node.isRed = false;
        }

        if (depth >= 0) {
//            if (random.nextDouble() >= leafProbability && depth >= 0) {
            // not necessarily always have left and right
            int delta = 1;
            if (node.isRed) {
                delta = 0;
            }
            node.setLeft(generateIntervalAux(random, tree, depth-delta, min, datum));
            node.setRight(generateIntervalAux(random, tree, depth-delta, datum, max));
        }
        return node;
    }

    @Override
    public RedBlackTree generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        int sz = random.nextInt(0,N );
        RedBlackTree tree = new RedBlackTree(Comparator.naturalOrder());
        //write code to generate a rbtree by populating data, color randomly and by calling generate
        // for left and right subtrees with some probability.
//        tree.setRoot(generateAux(random, tree, sz));
        tree.setRoot(generateIntervalAux(random, tree, sz, -K, K));
        assumeTrue(isValidRedBlackTree(tree));
        return tree;
    }

}
