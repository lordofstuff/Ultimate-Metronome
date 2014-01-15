package com.Stephen.ultimatemetronome;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

//import android.util.Log;

public class CustomLinkedList<T> extends AbstractList<T> {
	static String Tag = "CustomLinkedList";

	//fields
	DLNode front;
	DLNode back;
	int size;

	//constructors
	public CustomLinkedList() {
		super();
		front = null;
		back = null;
		size = 0;
	}


	//methods

	@Override
	public T get(int index) {
		if (index + 1 > size || index < 0) {
			throw new IndexOutOfBoundsException();
		}
		Iterator<T> it = iterator();
		T current = null;
		//Log.d(Tag, "Ready to start iterating");
		for(int i=0;i<index + 1;i++){
			current = it.next();
		}
		return current;

	}

	@Override
	public int size() {
		return size;
	}

	public int sizeForceCheck() {
		int counter = 0;
		for (@SuppressWarnings("unused") T t: this) {
			counter++;
		}
		size = counter;
		return counter;
	}

	public boolean isEmpty() {
		return (size == 0);
	}

	private DLNode getFront() {
		return front;
	}

	private DLNode getBack() {
		return back;
	}

	private void setFront(DLNode front) {
		this.front = front;
	}

	private void setBack(DLNode back) {
		this.back = back;
	}

	/**
	 * Moves an element from index from to index to. Does not alter length. 
	 * @param from
	 * @param to
	 * @throws IndexOutOfBoundsException
	 */
	public void moveElement(int from, int to) throws IndexOutOfBoundsException {
		if (from + 1 > size || from < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (to + 1 > size || to < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (to == from) {
			return;
		}
		DLNode currentNode;
		//decide whether to do it from front or from back
		if (from < to) { //moving forward, do from front
			currentNode = getFront();
			for(int i=0;i<from;i++) {
				currentNode = currentNode.getNext();
			}
			DLNode moveNode = currentNode;
			if (currentNode.getPrevious() != null) { //if it is not at the beginning of the list
				currentNode.getPrevious().setNext(currentNode.getNext());
			}
			else { //it was at the beginning of the list, and front must be updated
				setFront(currentNode.getNext());
			}
			currentNode.getNext().setPrevious(currentNode.getPrevious());
			for(int i=0;i< to - from ;i++) {//iterate up to the node before where it will be inserted
				currentNode = currentNode.getNext();
			}
			moveNode.setNext(currentNode.getNext());
			moveNode.setPrevious(currentNode);
			currentNode.setNext(moveNode);
			if (moveNode.getNext() != null) {
				moveNode.getNext().setPrevious(moveNode);
			}
			else {
				setBack(moveNode);
			}
		}
		else { //do it from the back
			currentNode = getBack();
			for(int i=0;i< size - from -1 ;i++) { //TODO check for off by one here. 
				currentNode = currentNode.getPrevious();
			}
			DLNode moveNode = currentNode;
			if (currentNode.getNext() != null) { //if it is not at the end of the list
				currentNode.getNext().setPrevious(currentNode.getPrevious());
			}
			else { //it was at the end of the list, and back must be updated
				setBack(currentNode.getPrevious());
			}
			currentNode.getPrevious().setNext(currentNode.getNext());//works even if null
			for(int i=0; i < from - to; i++) {//iterate up to the node after where it will be inserted
				currentNode = currentNode.getPrevious();
			}
			moveNode.setNext(currentNode);
			moveNode.setPrevious(currentNode.getPrevious());
			currentNode.setPrevious(moveNode);
			if (moveNode.getPrevious() != null) {
				moveNode.getPrevious().setNext(moveNode);
			}
			else {
				setFront(moveNode);
			}
		}

	}
	
	@Override
	public T remove(int index) {
		DLNode currentNode = getFront();
		for (int i = 0; i<index; i++) {
			currentNode = currentNode.getNext();
		}
		//now that we are pointing to the correct node, we will link the surrounding ones together, get the element, and null this node out. 
		if (currentNode.getPrevious() != null) {
			currentNode.getPrevious().setNext(currentNode.getNext());
		}
		else {
			setFront(currentNode.getNext());
		}
		if (currentNode.getNext() != null) {
			currentNode.getNext().setPrevious(currentNode.getPrevious());
		}
		else {
			setBack(currentNode.getPrevious());
		}
		size--;
		return currentNode.getElement();		
	}

	/**
	 * Adds the element to the back of the list. 
	 */
	@Override
	public boolean add(T element) {
		//creates a new node, sets it to the back to point to it, and sets this as the new back.
		back = new DLNode(element, back, null);
		if (back.getPrevious() == null) {
			front = back;
		}

		size++;
		return true; //TODO check what this means.
	}


	@Override
	public Iterator<T> iterator() {
		return new DLIterator();
	}

	public DLIterator iterator(boolean backwards) {
		return new DLIterator(backwards);
	}




	//private inner classes
	/**
	 * An inner class which is the iterator for this containing class. 
	 * @author Stephen Rodriguez
	 *
	 */
	public class DLIterator implements Iterator<T> {

		private DLNode nodePointer; 
		//frontPointer is a temporary node that just points to the front of the list, so that it does not need to read ahead. 
		private DLNode frontPointer;
		private DLNode backPointer;


		/**
		 * creates a new iterator at the front of the list.
		 */
		public DLIterator() {
			this(false);
		}

		/**
		 * Creates an iterator starting at either the front or the back of the list. 
		 * @param backwards true: Starts at back of list. false: starts at beginning of list. 
		 */
		public DLIterator(boolean backwards) {
			if (backwards) {
				backPointer = new DLNode(null);
				backPointer.setPrevious(CustomLinkedList.this.getBack());
				nodePointer = backPointer;
			}
			else {
				frontPointer = new DLNode(null);
				frontPointer.setNext(CustomLinkedList.this.getFront());
				nodePointer = frontPointer;
			}
		}

		@Override
		public T next() {
			nodePointer = nodePointer.getNext();
			return nodePointer.getElement();
		}

		/**
		 * Returns the element of the previous node in the list. 
		 * @return The element of the previous node in the list. 
		 */
		public T previous() {
			nodePointer = nodePointer.getPrevious();
			return nodePointer.getElement();
		}

		@Override
		public boolean hasNext() {
			return (nodePointer.getNext() != null);
		}

		/**
		 * Determines whether or not there is another element before the last one returned by next() or previous().
		 * @return whether a previous element exists. 
		 */
		public boolean hasPrevious() {
			return (nodePointer.getPrevious() != null);
		}

		public T current() {
			return nodePointer.getElement();
		}
		
		/**
		 * Sets the iterator to point to the passed element. One can continue iterating from there in either direction. 
		 * @param element the element it should point to.
		 * @throws NoSuchElementException if this exact element (instance equality) does not exist in the list. 
		 */
		public void set(T element) throws NoSuchElementException {
			//first, find the node containing this element:
			DLNode node = getFront();
			while (node != null) {
				if (node.getElement() == element) {
					//once one is found (it will take the first one it finds) it will set the iterator to point to it.
					nodePointer = node;
					return;
				}
				node = node.getNext();
			}
			//if it reaches this point, the element isn't in the list.
			throw new NoSuchElementException("No such element in list.");
		}
		
		/**
		 * Not supported. Will throw an exception. 
		 */
		@Override
		public void remove() {
			//TODO possibly don't support this
//			if (nodePointer == frontPointer || nodePointer == backPointer) {
//				//next() has never been called. throw IllegalStateException
//				throw new IllegalStateException("you must call next() or previous() before calling remove()");
//			}
//			else {  //will leave nodePointer on the removed node, which still points where it should, so that next() will work correctly
//				//set the previous pointers on the next node
//				if (nodePointer.getNext() != null) {
//					nodePointer.getNext().setPrevious(nodePointer.getPrevious());
//				}
//				else { //if it is at the end
//					CustomLinkedList.this.setBack(nodePointer.getPrevious());
//					nodePointer.getPrevious().setNext(null);
//				}
//				//set the next pointers on the previous node
//				if (nodePointer.getPrevious() != null) {
//					nodePointer.getPrevious().setNext(nodePointer.getNext());
//				}
//				else { //if at front
//					CustomLinkedList.this.setFront(nodePointer.getNext());
//					nodePointer.getNext().setPrevious(null);
//				}
//				size--;
//			}  
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * The node of a double linked list.
	 * @author Stephen Rodriguez
	 */
	private class DLNode {
		/**
		 * The element stored in the node.
		 */
		T element;

		/**
		 * A pointer to the next node in the list.
		 */
		DLNode next;

		/**
		 * A pointer to the previous node of the list.
		 */
		DLNode previous;

		/**
		 * The constructor.  Creates a node and puts it into place in the double linked list.
		 * @param element  the element to be stored in the node
		 * @param previous the node that will be before this node in the list, or null if no node is before this one
		 * @param next  the node that will be after this node in the list, or null of no node will be after this node
		 */
		DLNode(T element, DLNode previous, DLNode next) {
			this.element = element;
			this.next = next;
			this.previous = previous;
			if (next != null) {
				next.setPrevious(this);
			}
			if (previous != null) {
				previous.setNext(this);
			}
		}

		/**
		 * creates a new node without linking it to anything. Linking must be handled outside this constructor. 
		 * @param element The element of the new node. 
		 */
		DLNode(T element) {
			this(element, null, null);
		}

		/**
		 * Get the element stored in this node.
		 * @return the element stored in the node
		 */
		T getElement() {
			return element;
		}

		/**
		 * Gets the node that is before this node in the list.
		 * @return  a reference to the node that comes before this node in the list
		 */
		DLNode getPrevious() {
			return previous;
		}

		/**
		 * Gets the node that is after this node in the list.
		 * @return  a reference to the node that comes after this node in the list
		 */
		DLNode getNext() {
			return next;
		}

		/**
		 * Sets the reference to the node that will be after this node in the list.
		 * @param node  a reference to the node that will be after this node in the list
		 */
		void setNext(DLNode node) {
			next = node;
		}

		/**
		 * Sets the reference to the node that will be before this node in the list.
		 * @param node  a reference to the node that will be before this node in the list
		 */
		void setPrevious(DLNode node) {
			previous = node;
		}

		public String toString() {
			return "DLNode " + getElement().toString();
		}
	}
}
