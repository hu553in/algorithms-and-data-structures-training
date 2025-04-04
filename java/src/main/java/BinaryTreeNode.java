import lombok.Getter;
import lombok.Setter;

@Getter
public class BinaryTreeNode<T> {

    private final T value;
    @Setter
    private BinaryTreeNode<T> parent;
    private BinaryTreeNode<T> leftChild;
    private BinaryTreeNode<T> rightChild;

    public BinaryTreeNode(T value) {
        this.value = value;
    }

    public void removeParent() {
        parent = null;
    }

    public void setLeftChild(BinaryTreeNode<T> leftChild) {
        this.leftChild = leftChild;
        if (leftChild != null) {
            leftChild.setParent(this);
        }
    }

    public void setRightChild(BinaryTreeNode<T> rightChild) {
        this.rightChild = rightChild;
        if (rightChild != null) {
            rightChild.setParent(this);
        }
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasLeftChild() {
        return leftChild != null;
    }

    public boolean hasRightChild() {
        return rightChild != null;
    }
}
