
import static org.junit.Assert.*;
import static org.junit.Assume.*;


import org.junit.runner.RunWith;
import com.pholser.junit.quickcheck.*;
import com.pholser.junit.quickcheck.generator.*;
import edu.berkeley.cs.jqf.fuzz.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
