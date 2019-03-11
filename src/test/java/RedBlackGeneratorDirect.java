
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import javax.imageio.metadata.IIOInvalidTreeException;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import static org.junit.Assume.assumeTrue;

public class RedBlackGeneratorDirect extends Generator<RedBlackTree> {
    public RedBlackGeneratorDirect() { super(RedBlackTree.class); }

    public final int K = 100;
    public final int N = 100;
    public boolean valid = true;

    public boolean isValidRedBlackTree(RedBlackTree tree) {
        BinaryTreeNode root = tree.getRoot();
        // Check if it is a valid binary search tree.
        if (!isValidBST(tree)) {
            return false;
        }

        // Check if all paths through the tree have the same number of black nodes.
        if (!consistentPathLength(tree)) {
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

    public boolean consistentPathLength(RedBlackTree tree) {
        BinaryTreeNode.Visitor v = new BinaryTreeNode.Visitor() {
            int depth = -1;
            @Override
            public <E> void visit(BinaryTreeNode<E> node) {
                if (node.getLeft() == null && node.getRight() == null) {
                    int leafDepth = pathLength(tree, (RedBlackTree.Node) node);
                    if (depth == -1) {
                        depth = leafDepth;
                    } else if (depth != leafDepth) {
                        valid = false;
                    }

                }
            }
        };
        tree.root.traverseInorder(v);
        return valid;
    }

    public int pathLength(RedBlackTree tree, RedBlackTree.Node node) {
        // if root
        if (node == tree.getRoot()) {
            return 1;
        }
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
                if (((RedBlackTree.Node) node).isRed) {
                    if ( ((RedBlackTree.Node) node.getLeft()).isRed ||
                            ((RedBlackTree.Node) node.getRight()).isRed  ) {
                        valid = false;
                    }
                }
            }
        };
        tree.root.traversePreorder(v);
        return valid;
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

class InvalidTreeException extends Exception{}