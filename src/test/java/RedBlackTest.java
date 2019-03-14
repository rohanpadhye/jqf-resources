import com.pholser.junit.quickcheck.From;
import com.pholser.junit.quickcheck.generator.Size;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(JQF.class)
public class RedBlackTest {

    @Fuzz
    public void testAdd(@From(RedBlackGenerator.class) RedBlackTree tree, int d) {
        tree.add(d);
        assertTrue(tree.contains(d));
    }

    @Fuzz
    public void testRemove(@From(RedBlackGenerator.class) RedBlackTree tree, int d) {
        tree.remove(d);
        assertFalse(tree.contains(d));
    }

    @Fuzz
    public void testUnion(@Size(max=10) List<@From(RedBlackGenerator.class) RedBlackTree> trees) {
        RedBlackTree union = new RedBlackTree();
        for (RedBlackTree tree: trees) {
            BinaryTreeNode.Visitor v = new BinaryTreeNode.Visitor() {
                @Override
                public <E> void visit(BinaryTreeNode<E> node) {
                    union.add(node.getData());
                }
            };
            tree.root.traversePreorder(v);
        }

        for (RedBlackTree tree: trees) {
            BinaryTreeNode.Visitor v = new BinaryTreeNode.Visitor() {
                @Override
                public <E> void visit(BinaryTreeNode<E> node) {
                    assertTrue(union.contains(node.getData()));
                }
            };
            tree.root.traversePreorder(v);
        }
    }
}
