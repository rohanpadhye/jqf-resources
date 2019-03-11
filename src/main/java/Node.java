/**
 * The nodes in a red-black tree store a color together with the actual data
 * in the node.
 */
public class Node extends LinkedBinaryTreeNode {
    boolean isRed = false;

    public Node(Object data) {
        super(data);
    }
}