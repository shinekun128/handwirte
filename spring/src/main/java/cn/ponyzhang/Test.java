package cn.ponyzhang;

import cn.ponyzhang.service.BookService;
import cn.spring.PonyZhangApplicationContext;

import java.awt.print.Book;

public class Test {
    public static void main(String[] args) {
        PonyZhangApplicationContext applicationContext = new PonyZhangApplicationContext(AppConfig.class);
        BookService bookService = (BookService) applicationContext.getBean("bookService");
        bookService.test();



    }
}
