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
public class RedBlackDirectTest {

    @Fuzz
    public void testAdd(@From(RedBlackGeneratorDirect.class) RedBlackTree tree, int d) {
        tree.add(d);
        assertTrue(tree.contains(d));
    }

    @Fuzz
    public void testRemove(@From(RedBlackGeneratorDirect.class) RedBlackTree tree, int d) {
        tree.remove(d);
        assertFalse(tree.contains(d));
    }

    @Fuzz
    public void testUnion(@Size(max=10) List<@From(RedBlackGeneratorDirect.class) RedBlackTree> trees) {
        RedBlackTree union = new RedBlackTree(Comparator.naturalOrder());
        for (RedBlackTree tree: trees) {
            BSTIterator v = new BSTIterator();
            tree.root.traversePreorder(v);
            for (Object o:  v.history) {
                union.add(o);
            }
        }

        for (RedBlackTree tree: trees) {
            BSTIterator v = new BSTIterator();
            tree.root.traversePreorder(v);
            for (Object o:  v.history) {
                assumeTrue(union.contains(o));
            }
        }
//        assumeTrue()
    }

    public class BSTIterator implements BinaryTreeNode.Visitor {
        public List<Object> history = new LinkedList<>();
        @Override
        public <E> void visit(BinaryTreeNode<E> node) {
            history.add(node.getData());
        }
    }
}
