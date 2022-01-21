import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Solutions {
    /**
     * Validate different types of brackets — (), [], {} — via stack
     */
    public boolean validateDifferentBracketsViaStack(final String string) {
        if (string == null) {
            return false;
        }
        var closingForOpening = Map.of(
                '(', ')',
                '[', ']',
                '{', '}'
        );
        var stack = new ArrayDeque<Character>();
        var chars = string.toCharArray();
        for (var newChar : chars) {
            if (closingForOpening.containsKey(newChar)) {
                stack.push(newChar);
            } else {
                if (stack.isEmpty()) {
                    return false;
                }
                char closingForNewChar = stack.pop();
                var expectedClosing = closingForOpening.get(closingForNewChar);
                if (newChar != expectedClosing) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validate round brackets only — () — via MapReduce (without stack)
     */
    public boolean validateRoundBracketsViaMapReduce(
            final String string,
            final int pieceLength
    ) throws InterruptedException {
        if (string == null) {
            return false;
        }
        var pieceCount = (int) Math.ceil((double) string.length() / pieceLength);
        var executor = Executors.newFixedThreadPool(pieceCount);
        try {
            var total = new AtomicLong();
            var deviations = new long[pieceCount];
            var countDownLatch = new CountDownLatch(pieceCount);
            for (var i = 0; i < pieceCount; i++) {
                final var finalIndex = i;
                executor.submit(() -> {
                    var pieceEndExclusiveIndex = pieceLength * (finalIndex + 1);
                    if (pieceEndExclusiveIndex > string.length()) {
                        pieceEndExclusiveIndex = string.length();
                    }
                    var piece = string.substring(
                            pieceLength * finalIndex,
                            pieceEndExclusiveIndex
                    ).toCharArray();
                    var maxDeviation = 0L;
                    var localTotal = 0L;
                    for (var character : piece) {
                        if (character == '(') {
                            localTotal++;
                        } else if (character == ')') {
                            localTotal--;
                        }
                        if (Math.abs(localTotal) > Math.abs(maxDeviation)) {
                            maxDeviation = localTotal;
                        }
                    }
                    deviations[finalIndex] = maxDeviation;
                    total.addAndGet(localTotal);
                    countDownLatch.countDown();
                });
            }
            countDownLatch.await();
            var currentDeviation = 0;
            for (var newDeviation : deviations) {
                currentDeviation += newDeviation;
                if (currentDeviation < 0) {
                    return false;
                }
            }
            return total.get() == 0;
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Concurrent merge sort
     */
    public <T extends Comparable<T>> T[] concurrentMergeSort(final T[] array) throws Exception {
        if (array == null || Arrays.stream(array).anyMatch(Objects::isNull)) {
            return null;
        }
        var executor = Executors.newCachedThreadPool();
        try {
            return executor.submit(() -> {
                var length = array.length;
                if (length > 1) {
                    var middle = length / 2;
                    var left = concurrentMergeSort(Arrays.copyOfRange(array, 0, middle));
                    var right = concurrentMergeSort(Arrays.copyOfRange(array, middle, length));
                    var leftIndex = 0;
                    var rightIndex = 0;
                    var arrayIndex = 0;
                    while (leftIndex < left.length && rightIndex < right.length) {
                        if (left[leftIndex].compareTo(right[rightIndex]) < 0) {
                            array[arrayIndex] = left[leftIndex];
                            leftIndex++;
                        } else if (left[leftIndex].compareTo(right[rightIndex]) > 0) {
                            array[arrayIndex] = right[rightIndex];
                            rightIndex++;
                        }
                        arrayIndex++;
                    }
                    while (leftIndex < left.length) {
                        array[arrayIndex] = left[leftIndex];
                        leftIndex++;
                        arrayIndex++;
                    }
                    while (rightIndex < right.length) {
                        array[arrayIndex] = right[rightIndex];
                        rightIndex++;
                        arrayIndex++;
                    }
                }
                return array;
            }).get();
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Column addition of two numbers stored in linked lists with first elements as minor ones, e.g.:
     * 876 + 7654 = 8530
     * [ 6, 7, 8 ]    +
     * [ 4, 5, 6, 7 ] =
     * [ 0, 3, 5, 8 ]
     */
    public LinkedList<Integer> sumTwoNumbers(
            final LinkedList<Integer> first,
            final LinkedList<Integer> second
    ) {
        if (first == null || second == null || (first.isEmpty() && second.isEmpty())) {
            return null;
        }
        if (first.isEmpty()) {
            return second;
        }
        if (second.isEmpty()) {
            return first;
        }
        var sum = new LinkedList<Integer>();
        var remainder = 0;
        for (int i = 0, j = 0; i < first.size() || j < second.size(); i++, j++) {
            var firstDigit = i < first.size() ? first.get(i) : 0;
            var secondDigit = j < second.size() ? second.get(j) : 0;
            var sumDigit = firstDigit + secondDigit + remainder;
            if (sumDigit > 9) {
                remainder = 1;
                sumDigit %= 10;
            }
            sum.add(sumDigit);
        }
        return sum;
    }

    /**
     * Sort array of fixed number set — e.g. 0, 1, 2 — with O(n) complexity
     */
    public int[] sortArrayOfFixedNumberSet(int[] array) {
        if (array == null) {
            return null;
        }
        var countingMap = new TreeMap<Integer, Integer>();
        for (var elem : array) {
            countingMap.compute(elem, (k, v) -> v == null ? 1 : v + 1);
        }
        var arrayIndex = new AtomicInteger();
        countingMap.forEach((k, v) -> {
            for (var i = 0; i < v; i++, arrayIndex.getAndIncrement()) {
                array[arrayIndex.get()] = k;
            }
        });
        return array;
    }

    /**
     * There is queue for tickets.
     * Each person requires certain number of tickets.
     * Single person can buy only one ticket at single time.
     * If more is required, then person goes to end of queue with number of required tickets reduced by 1.
     * If person has bought all required tickets, then they leave queue and queue is shortened.
     * New people do not appear in queue.
     * Task is to calculate number of iterations of ticket purchases for target person to buy all tickets they require.
     *
     * @param requiredTicketCounts numbers of tickets that each person requires
     * @param targetIndex          index of target person in queue
     */
    public int tickets(final int[] requiredTicketCounts, final int targetIndex) {
        if (requiredTicketCounts == null || requiredTicketCounts.length == 0 || targetIndex < 0) {
            return -1;
        }
        var queueLength = requiredTicketCounts.length - 1;
        var iterations = 0;
        var mutableTargetIndex = targetIndex;
        while (true) {
            iterations++;
            var requiredTicketCount = requiredTicketCounts[0] - 1;
            System.arraycopy(requiredTicketCounts, 1, requiredTicketCounts, 0, queueLength);
            requiredTicketCounts[queueLength] = requiredTicketCount;
            if (requiredTicketCount > 0) {
                if (mutableTargetIndex == 0) {
                    mutableTargetIndex = queueLength;
                } else {
                    mutableTargetIndex--;
                }
            } else {
                if (mutableTargetIndex == 0) {
                    return iterations;
                }
                mutableTargetIndex--;
                queueLength--;
            }
        }
    }

    /**
     * Convert binary search tree to list
     */
    public <T> List<T> convertBinarySearchTreeToList(final BinaryTreeNode<T> rootNode) {
        if (rootNode == null) {
            return null;
        }
        var result = new LinkedList<T>();
        var mutableRootNode = rootNode;
        var currentNode = mutableRootNode;
        while (currentNode.hasRightChild() || currentNode.hasLeftChild() || currentNode.hasParent()) {
            while (currentNode.hasLeftChild()) {
                currentNode = currentNode.getLeftChild();
            }
            result.add(currentNode.getValue());
            if (currentNode.hasRightChild()) {
                if (currentNode.hasParent()) {
                    currentNode.getParent().setLeftChild(currentNode.getRightChild());
                } else {
                    mutableRootNode = currentNode.getRightChild();
                    mutableRootNode.removeParent();
                }
            } else if (currentNode.hasParent()) {
                currentNode.getParent().setLeftChild(null);
            }
            currentNode = mutableRootNode;
        }
        result.add(currentNode.getValue());
        return result;
    }

    /**
     * Convert binary search tree to list with recursion
     */
    public <T> List<T> convertBinarySearchTreeToListWithRecursion(final BinaryTreeNode<T> rootNode) {
        var result = new LinkedList<T>();
        if (rootNode != null) {
            result.addAll(convertBinarySearchTreeToListWithRecursion(rootNode.getLeftChild()));
            result.add(rootNode.getValue());
            result.addAll(convertBinarySearchTreeToListWithRecursion(rootNode.getRightChild()));
        }
        return result;
    }

    /**
     * Check whether binary tree is binary search tree
     */
    public <T extends Comparable<T>> boolean isBinaryTreeSearch(
            final BinaryTreeNode<T> rootNode,
            final T lowerBoundExclusive,
            final T upperBoundExclusive
    ) {
        if (rootNode != null) {
            var rootNodeValue = rootNode.getValue();
            if (lowerBoundExclusive != null && rootNodeValue.compareTo(lowerBoundExclusive) <= 0) {
                return false;
            }
            if (upperBoundExclusive != null && rootNodeValue.compareTo(upperBoundExclusive) >= 0) {
                return false;
            }
            var leftChild = rootNode.getLeftChild();
            if (leftChild != null &&
                    (leftChild.getValue().compareTo(rootNodeValue) >= 0 ||
                            !isBinaryTreeSearch(leftChild, lowerBoundExclusive, rootNodeValue))) {
                return false;
            }
            var rightChild = rootNode.getRightChild();
            if (rightChild != null) {
                return rightChild.getValue().compareTo(rootNodeValue) > 0 &&
                        isBinaryTreeSearch(rightChild, rootNodeValue, upperBoundExclusive);
            }
        }
        return true;
    }

    /**
     * Merge list of sorted producer queues into single sorted consumer queue
     */
    public <T extends Comparable<T>> CompletableFuture<Void> mergeSortedProducersIntoSingleSortedConsumer(
            final List<? extends Queue<T>> producers,
            final Queue<T> consumer,
            final Comparator<T> comparator
    ) {
        if (producers == null || producers.isEmpty() || consumer == null) {
            return CompletableFuture.failedFuture(new Exception());
        }
        return CompletableFuture.runAsync(() -> {
            var buffer = new TreeMap<T, List<Integer>>(comparator);
            for (var i = 0; i < producers.size(); i++) {
                var element = producers.get(i).poll();
                if (element != null) {
                    if (buffer.containsKey(element)) {
                        buffer.get(element).add(i);
                    } else {
                        final int producerIndex = i;
                        buffer.put(element, new LinkedList<>() {{
                            add(producerIndex);
                        }});
                    }
                }
            }
            var offerResult = true;
            while (!buffer.isEmpty() && offerResult) {
                var min = buffer.firstEntry();
                if (min != null) {
                    offerResult = consumer.offer(min.getKey());
                    var producerIndex = min.getValue().remove(0);
                    if (min.getValue().isEmpty()) {
                        buffer.remove(min.getKey());
                    }
                    var element = producers.get(producerIndex).poll();
                    if (element != null) {
                        if (buffer.containsKey(element)) {
                            buffer.get(element).add(producerIndex);
                        } else {
                            buffer.put(element, new LinkedList<>() {{
                                add(producerIndex);
                            }});
                        }
                    }
                }
            }
        });
    }

    /**
     * Mirror binary tree
     */
    public <T> BinaryTreeNode<T> mirrorBinaryTree(final BinaryTreeNode<T> rootNode) {
        if (rootNode != null) {
            var temp = rootNode.getLeftChild();
            rootNode.setLeftChild(rootNode.getRightChild());
            rootNode.setRightChild(temp);
            mirrorBinaryTree(rootNode.getLeftChild());
            mirrorBinaryTree(rootNode.getRightChild());
        }
        return rootNode;
    }

    /**
     * Find unpaired element in array of paired elements, e.g.:
     * [ 1, 1, 5, 3, 3, 4, 4 ] -> 2
     */
    public <T> int findUnpairedElementInArrayOfPairedElements(final T[] array) {
        if (array == null || array.length % 2 == 0) {
            return -1;
        }
        if (array.length == 1) {
            return 0;
        }
        var startIndex = 0;
        var endIndex = array.length - 1;
        while (startIndex != endIndex) {
            var middle = (startIndex + endIndex + 1) / 2;
            if (array[middle] == array[middle - 1]) {
                if ((middle - startIndex) % 2 == 0) {
                    endIndex = middle - 1;
                } else {
                    startIndex = middle + 1;
                }
            } else if (array[middle] == array[middle + 1]) {
                if ((endIndex - middle) % 2 == 0) {
                    startIndex = middle + 1;
                } else {
                    endIndex = middle - 1;
                }
            } else {
                return middle;
            }
        }
        return startIndex;
    }
}
