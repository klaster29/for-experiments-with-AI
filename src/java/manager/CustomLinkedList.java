package manager;

import task.Task;

import java.util.*;

class CustomLinkedList {

    private final Map<Integer, Node> list;
    private Node first;
    private Node last;

    CustomLinkedList() {
        list = new HashMap<>();
    }

    void remove(int id) {
        if (list.containsKey(id)) {
            Node currentNode = list.get(id);
            Node prevNode = currentNode.getPrev();
            Node nextNode = currentNode.getNext();

            setPrevNodeWithConsists(prevNode, nextNode);
            setNextNodeWithConsists(prevNode, nextNode);

            list.remove(id);
        }
    }

    void linkLast(Task task) {
        Node currentNode;
        if (list.isEmpty()) {
            currentNode = new Node(task, null, null);
            first = currentNode;
        } else {
            currentNode = new Node(task, last, null);
            if (list.containsKey(task.getId())) {
                removeNode(list.get(task.getId()));
            }
            currentNode.getPrev().setNext(currentNode);

        }
        last = currentNode;
        list.put(task.getId(), currentNode);
    }

    List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node currentNode = first;
        while (currentNode != null) {
            result.add(currentNode.task);
            currentNode = currentNode.next;
        }
        return List.copyOf(result);
    }

    private void setPrevNodeWithConsists(Node prevNode, Node nextNode) {
        if (prevNode != null) {
            if (nextNode != null) {
                prevNode.setNext(nextNode);
            } else {
                prevNode.setNext(null);
                last = prevNode;
            }
        }
        if (list.size() == 1) {
            last = prevNode;
        }
    }

    private void setNextNodeWithConsists(Node prevNode, Node nextNode) {
        if (nextNode != null) {
            if (prevNode != null) {
                nextNode.setPrev(prevNode);
            } else {
                nextNode.setPrev(null);
                first = nextNode;
            }
        }
        if (list.size() == 1) {
            first = nextNode;
        }
    }

    private void removeNode(Node node) {
        if (list.size() == 1 && list.containsKey(node.task.getId())) {
            return;
        }
        setPrevNodeWithConsists(node.getPrev(), node.getNext());
        setNextNodeWithConsists(node.getPrev(), node.getNext());
        list.remove(node.task.getId());
    }

    private static class Node {
        private final Task task;
        private Node prev;
        private Node next;

        public Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }

        public Node getPrev() {
            return prev;
        }

        public void setPrev(Node prev) {
            this.prev = prev;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }
}