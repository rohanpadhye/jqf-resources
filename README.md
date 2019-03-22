# Stateful Fuzzing Tutorial

## Introduction
In this tutorial, we will be going through writing a stateful data structure generator. This generator can be used with JQF to fuzz programs to find inputs that would cause bugs and to improve test code coverage. To read more about JQF, you can refer to [JQF's readme](https://github.com/rohanpadhye/jqf).

We will begin by using an implementation of a Red Black Tree (http://cs.lmu.edu/~ray/notes/redblacktrees/), and our goal is to use fuzzing to test the correctness of the implementation. Zest performs generator-based mutational fuzzing, with feedback from code coverage and input validity. For a specific application, this requires building a parametric generator of the object, as well as a test driver.

There are two ways of implementing a generator for a data structure that we will examine. The first, called **stateful generation**, is one that generates an intermediate representation of the structure that might not be valid. The second, called **sequence-based generation** is one that uses the data structure's implementation to transform the data structure, such as using its `add` and `remove` functionality. A benefit of the former is that it does not rely on the correctness of the implementation, and a benefit of the latter is that it is generally easier to write.

## Requirements
To run this tutorial, you will need to have installed Java 8+, Apache Maven, and bash to run from the command line. This tutorial was tested on Ubuntu 16.04, but may also work on Mac OS and Windows.

Next, you can clone this repository by running
```
git clone git@github.com:rohanpadhye/android-fuzzing.git
```
All of the commands from now onwards can be run from the directory created, `/path/to/android-fuzzing`.

Another dependency this relies on is the [JQF Maven Plugin](https://github.com/rohanpadhye/jqf/wiki/JQF-Maven-Plugin). You will not have to download this yourself: it is listed as a dependency in the [pom.xml](https://github.com/rohanpadhye/android-fuzzing/blob/master/pom.xml) under plugins.

To build and run the fuzzer, you can run:
```
mvn jqf:fuzz -Dclass=RedBlackDirectTest -Dmethod=testAdd
```
You can also try it with `-Dmethod=testRemove` and `-Dmethod=testUnion` to try out the other tests.

## Test Driver
The tests that we will be using to verify correctness of the given Red Black Tree will test adding elements, removing elements, and taking the union of multiple trees. These tests are effectively testing the set-like properties of a Red Black Tree, and do not test the performance of the implementation, just correctness.

```java
@Fuzz
public void testAdd(@From(RedBlackGeneratorDirect.class) RedBlackTree tree, int d) {
    assumeTrue(isValidRedBlackTree(tree));
    tree.add(d);
    assertTrue(tree.contains(d));
    assertTrue(isValidRedBlackTree(tree));
}
```

The file containing `testAdd` can be found at [RedBlackDirectTest.java](https://github.com/rohanpadhye/android-fuzzing/blob/a9ab32ce1bf11e4eb29ea3698ea99cd91da5bc37/src/test/java/RedBlackDirectTest.java#L106), the `@Fuzz` annotation tells JQF which tests can be used for fuzzing, that which require generator inputs. The `testAdd` method takes in two arguments here, where the first is annotated: `@From(RedBlackGeneratorDirect.class) RedBlackTree tree`. Since the inputs to this test are a parametrically generated, we need to specify from which generator should this object be generated from. In this case, we are generating a RedBlackTree from the RedBlackGeneratorDirect class, that contains an implementation of the `generate` method. The second argument, `int d`, does not require a special annotation because JQF already contains a generator for `int` type primitives, and this is the one we want to use.

Inside the method body, we start with our test's precondition, using JUnit's `assumeTrue`. Our precondition for this test is that the tree must be a valid Red Black Tree, which means that it satisfies the invariants that all valid Red Black Trees must satisfy. This is necessary because it may be possible that our generator does not generate semantically valid red black trees. If it is not valid, then JQF will throw this input away, and try to generate another one that is valid. Next, our test logic runs, in this case adding an element. Finally, our test's postconditions are asserted with `assertTrue`. This asserts that the tree does contain the element we just added, and that the tree is still a valid Red Black Tree.

Our test driver uses the method `isValidRedBlackTree`, which tests the three invariants that a valid RedBlackTree must satisfy: it is a binary search tree, each red node must have a black parent and the root is black, and the number of black nodes in the path from the root to a null child is the same for every null child.

## Stateful Generator

In the Test Driver section, we noted that the RedBlackTree was generated from JQF from the class `RedBlackGeneratorDirect`. This class contains relevant methods for stateful generation of RedBlackTrees, in that generation is not purely random: some of the generation is constrained to enforce RedBlackTree invariants.

In particular, the method `generate` is called by JQF for generation. One of the parameters, `SourceOfRandomness random`, is the parameterization of the tree, and it is this parameter that is mutated by JQF during fuzzing.

```java
    @Override
    public RedBlackTree generate(SourceOfRandomness random, GenerationStatus __ignore__) {
        int treeDepth = random.nextInt(0,100);
        RedBlackTree tree = new RedBlackTree(Comparator.naturalOrder());
        tree.setRoot(generateIntervalAux(random, tree, treeDepth, -K, K));
        return tree;
    }
``` 

Here, we construct a new `RedBlackTree`, and set its root node to be `generateIntervalAux(random, tree, treeDepth, -K, K)`.

```java
    private RedBlackTree.Node generateIntervalAux(SourceOfRandomness random, RedBlackTree tree, int maxDepth, int min, int max) {
        int data = random.nextInt(min, max);
        RedBlackTree.Node node = tree.new Node(data);
        node.isRed = random.nextBoolean();

        if (random.nextBoolean() && maxDepth >= 0) {
            if (random.nextBoolean()) {
                node.setLeft(generateIntervalAux(random, tree, maxDepth - 1, min, data));
                node.setRight(generateIntervalAux(random, tree, maxDepth - 1, data, max));
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
```

`generateIntervalAux` generates a `RedBlackTree` that satisfies the Binary Search Tree constraint. Note element generation `data = random.nextInt(min, max)` enforces the BST invariant. It assigns the node colors randomly, and does not guarantee satisfaction of the node color invariants that a RedBlackTree must satisfy.

This method also enforces a maxDepth on the tree. Each node is randomly chosen to be a leaf, and if it is not, then it is randomly chosen to have one or two children, who are recursively generated.

<b>Do I need to enforce invariants in my generator?</b> 

In this example, we enforced some invariants during generation. This is not always necessary. If the invariants are easy to spell out in code, then adding them might help JQF find more valid inputs. Otherwise, if another invariant would be hard to enforce, then it is fine to leave it out. A good approach would be to start with a generator that does not enforce constraints. Here, we could have left out the BST constraint, and just generated random numbers at each node. You can try that out too: I left [generateAux](https://github.com/rohanpadhye/android-fuzzing/blob/e3caf622fc8890f1d8f43ea857ba6e40162ac3eb/src/test/java/RedBlackGeneratorDirect.java#L21) in RedBlackGeneratorDirect: in [generate]() you can replace the usage of `generateIntervalAux` with `generateAux` and see the results. 

## Results

### Quick-check fuzzing (No feedback guidance)
The output of running 

```
mvn jqf:fuzz -Dclass=RedBlackDirectTest -Dmethod=testAdd -Dblind
```
is the following:

<pre><code>
Test name:            RedBlackDirectTest#testAdd
Results directory:    /home/sirej/projects/redblacktrees/target/fuzz-results/RedBlackDirectTest/testAdd
Elapsed time:         1m 20s (no time limit)
Number of executions: 227,099
Valid inputs:         <b>69,694 (30.69%)</b>
Cycles completed:     0
Unique failures:      0
Queue size:           0 (0 favored last cycle)
Current parent input: <seed>
Execution speed:      3,130/sec now | 2,819/sec overall
Total coverage:       <b>213 (0.33% of map)</b>
Valid coverage:       208 (0.32% of map)
</code></pre>

In 80 seconds, quick check was able to generate 69,684 valid inputs on my machine, and covered 213 branches.

### Zest fuzzing with feedback guidance
The output of running 

```
mvn jqf:fuzz -Dclass=RedBlackDirectTest -Dmethod=testAdd
```
is the following:

<pre><code>
Test name:            RedBlackDirectTest#testAdd
Results directory:    /home/sirej/projects/redblacktrees/target/fuzz-results/RedBlackDirectTest/testAdd
Elapsed time:         1m 20s (no time limit)
Number of executions: 193,203
Valid inputs:         <b>87,569 (45.32%)</b>
Cycles completed:     29
Unique failures:      0
Queue size:           35 (13 favored last cycle)
Current parent input: 15 (favored) {404/620 mutations}
Execution speed:      2,480/sec now | 2,406/sec overall
Total coverage:       <b>227 (0.35% of map)</b>
Valid coverage:       222 (0.34% of map)
</code></pre>

In 80 seconds, zest was able to generate 87,569 valid inputs on my machine, and covered 227 branches.

### Comparison
Though both QuickCheck and Zest both used the same generator as described above, Zest was able to outperform QuickCheck in terms of the number of branches covered, as well as the amount of valid inputs generated. Note that QuickCheck is faster in generating inputs: it generated 227,099 total inputs, which is greater than Zest's 193,203 total inputs. Yet, Zest was able to generate more valid inputs.

Another assessment of the difference between these two methods of fuzzing is to see what the difference is.

We can run 
```
mvn jqf:repro -Dclass=RedBlackDirectTest -Dmethod=testAdd -Dinput=target/fuzz-results/RedBlackDirectTest/testAdd/corpus -DlogCoverage=coverage.out
```
to see the total code coverage of the last JQF fuzz. Note that this should be run after each fuzz, once after quickcheck and once after zest, since one will overried the other. For example, a sequence of four commands would be:
```
mvn jqf:fuzz -Dclass=RedBlackDirectTest -Dmethod=testAdd -Dblind -Dtime=60s
mvn jqf:repro -Dclass=RedBlackDirectTest -Dmethod=testAdd -Dinput=target/fuzz-results/RedBlackDirectTest/testAdd/corpus -DlogCoverage=coverage_quickcheck.out
mvn jqf:fuzz -Dclass=RedBlackDirectTest -Dmethod=testAdd -Dtime=60s
mvn jqf:repro -Dclass=RedBlackDirectTest -Dmethod=testAdd -Dinput=target/fuzz-results/RedBlackDirectTest/testAdd/corpus -DlogCoverage=coverage_zest.out
```

Now, we can diff these two files with 
```
diff coverage_quickcheck.out coverage_zest.out
```
to see what the difference in coverage is. On my machine, I found that a branch in [BinarySearchTree.rotateLeft](https://github.com/rohanpadhye/android-fuzzing/blob/a9ab32ce1bf11e4eb29ea3698ea99cd91da5bc37/src/main/java/BinarySearchTree.java#L152) was not covered by QuickCheck, but was by Zest.

# Sequence-based Generator
As discussed in the [introduction](https://github.com/rohanpadhye/android-fuzzing/blob/master/README.md#introduction), an alternative generator is one that is sequence-based. A sequence-based generator would not handle the raw interior structure as the above stateful generator did, but would rather call implemented functions from the object to manipulate it. In this way, the parameters can be interpreted as a sequence of method calls. This would require less bookkeeping on the test writer's end, and would also result in inputs that are always semantically valid, assuming the implementation is correct. Note that the above stateful generator often generated invalid inputs.

The sequence generator in [RedBlackGenerator](https://github.com/rohanpadhye/android-fuzzing/blob/master/src/test/java/RedBlackGenerator.java) is written as follows:
```java
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
```
This generator uses the parameters to determine whether it should add or remove elements. This generator is much less complex; it is easier to reason about.

## Test driver
To run the fuzzer, we can take our previous test class, and change instances of `RedBlackGeneratorDirect` to `RedBlackGenerator`, to indicate that we want the generator to be `RedBlackGenerator` for these tests. This test class can be found in [RedBlackTest](https://github.com/rohanpadhye/android-fuzzing/blob/master/src/test/java/RedBlackTest.java).


## Results

### Sequence-based Fuzzing Without Feedback (QuickCheck)

<pre><code>
Test name:            RedBlackTest#testAdd
Results directory:    /home/sirej/projects/redblacktrees/target/fuzz-results/RedBlackTest/testAdd
Elapsed time:         1m 20s (no time limit)
Number of executions: 110,061
Valid inputs:         <b>110,061 (100.00%)</b>
Cycles completed:     0
Unique failures:      0
Queue size:           0 (0 favored last cycle)
Current parent input: <seed>
Execution speed:      1,443/sec now | 1,366/sec overall
Total coverage:       <b>170 (0.26% of map)</b>
Valid coverage:       170 (0.26% of map)
</code></pre>

This sequence-based generator did not generate a single invalid input in 80 seconds. This might indicate that the implementation whose methods `add` and `remove` we are calling creates semantically valid red black trees.


### Sequence-based Fuzzing With Feedback (Zest)

<pre><code>
Test name:            RedBlackTest#testAdd
Results directory:    /home/sirej/projects/redblacktrees/target/fuzz-results/RedBlackTest/testAdd
Elapsed time:         1m 20s (no time limit)
Number of executions: 80,548
Valid inputs:         <p>80,548 (100.00%)</p>
Cycles completed:     12
Unique failures:      0
Queue size:           24 (12 favored last cycle)
Current parent input: 14 (favored) {31/700 mutations}
Execution speed:      1,010/sec now | 1,002/sec overall
Total coverage:       <p>182 (0.28% of map)</p>
Valid coverage:       182 (0.28% of map)
</code></pre>

Since this is also using the same sequence-based generator, it also has only generated valid inputs. 

### Comparison
With the sequence based generator, we see that feedback-guided fuzzing (zest) covered 182 branches, whereas fuzzing without feedback (quickcheck) covered 170 branches.

We can log the coverage analysis to see what the difference was. Run:
```
mvn jqf:fuzz -Dclass=RedBlackTest -Dmethod=testAdd -Dblind -Dtime=60s
mvn jqf:repro -Dclass=RedBlackTest -Dmethod=testAdd -Dinput=target/fuzz-results/RedBlackTest/testAdd/corpus -DlogCoverage=coverage_sequence_quickcheck.out
mvn jqf:fuzz -Dclass=RedBlackTest -Dmethod=testAdd -Dtime=60s
mvn jqf:repro -Dclass=RedBlackTest -Dmethod=testAdd -Dinput=target/fuzz-results/RedBlackTest/testAdd/corpus -DlogCoverage=coverage_sequence_zest.out
```
Now, we can diff the two outputs files:
```
diff coverage_sequence_quickcheck.out coverage_sequence_zest.out
```

I found a difference between the coverage differences between the stateful generator from before and this sequence based generator, namely a branch in [add](https://github.com/rohanpadhye/android-fuzzing/blob/e3caf622fc8890f1d8f43ea857ba6e40162ac3eb/src/main/java/RedBlackTree.java#L52).
