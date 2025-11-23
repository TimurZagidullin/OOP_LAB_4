package com.example.lab_4_oop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class Storage<T> implements Iterable<T> { // <T> - это параметр типа (дженерик), означает "любой тип" // implements Iterable<T> - класс реализует интерфейс Iterable, что позволяет использовать цикл for-each
    private final List<T> storage = new ArrayList<>(); // Приватное поле storage - внутренний список для хранения элементов // new ArrayList<>() - создает новый пустой динамический массив

    // Метод add - добавляет элемент в хранилище
    public void add(T item) { // T item - параметр типа T (элемент, который нужно добавить)
        if (!storage.contains(item)) { // Проверка: если элемент еще не содержится в списке // contains() - метод списка, проверяет есть ли такой элемент
            storage.add(item); // Добавление элемента в конец списка
        }
    }

    // Метод remove - удаляет элемент из хранилища
    public void remove(T item) { // T item - элемент, который нужно удалить
        storage.remove(item); // storage.remove(item) - удаляет первое вхождение указанного элемента
    }

    // Метод clear - очищает хранилище (удаляет все элементы)
    public void clear() { storage.clear(); } // Очистка списка - удаление всех элементов

    // Метод size - возвращает количество элементов в хранилище
    public int size() { return storage.size(); }// Возвращает размер списка (сколько элементов в нем хранится)

    // Метод get - получает элемент по индексу (номеру позиции)
    public T get(int index) {return storage.get(index);}

    // Переопределение метода iterator - необходим для реализации интерфейса Iterable
    @Override
    public Iterator<T> iterator() { // @Override - указывает, что мы переопределяем метод из интерфейса  // Iterator<T> - возвращает итератор для перебора элементов
        return storage.iterator(); // Возвращает итератор внутреннего списка// Итератор позволяет перебирать элементы в цикле for-each // Например: for (Shape shape : storage) { ... }
    }
}