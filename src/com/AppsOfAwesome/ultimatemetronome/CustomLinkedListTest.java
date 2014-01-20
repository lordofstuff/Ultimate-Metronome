package com.AppsOfAwesome.ultimatemetronome;

import static org.junit.Assert.*;

import org.junit.Test;


public class CustomLinkedListTest {
  
  @Test
  public void testMove() {
    CustomLinkedList<Integer> list = resetList();
    subTest(list, 1,2,3,4);
    list.moveElement(1,2);
    subTest(list, 1,3,2,4);
    list.moveElement(1,2);
    subTest(list, 1,2,3,4);
    list.moveElement(2,1);
    subTest(list, 1,3,2,4);
    
    list = resetList();
    list.moveElement(3,0);
    subTest(list, 4,1,2,3);
    
    
    list = resetList();
    list.moveElement(0,3);
    subTest(list, 2,3,4,1);
    
    list = resetList();
    list.moveElement(1,1);
    subTest(list, 1,2,3,4);
    
    list = resetList();
    list.moveElement(2,3);
    subTest(list, 1,2,4,3);
    
    list = resetList();
    list.moveElement(1,3);
    subTest(list, 1,3,4,2);
    
    list = resetList();
    list.moveElement(0,3);
    subTest(list, 2,3,4,1);
    
  }
  
  private void subTest(CustomLinkedList<Integer> list, int... args) {
    int i1 = 0;
    for (int i2: list) {
      assertTrue("worked and next set correctly", i2 == args[i1++]);
    }
    CustomLinkedList<Integer>.DLIterator it = list.iterator(true);
    
    while (it.hasPrevious()) {
      assertTrue("worked and previous set correctly", it.previous() == args[--i1]);
    }
    
  }
  
  private CustomLinkedList<Integer> resetList() {
    CustomLinkedList<Integer> list = new CustomLinkedList<Integer>();
    list.add(1);
    list.add(2);
    list.add(3);
    list.add(4);
    return list;
  }
  
}
