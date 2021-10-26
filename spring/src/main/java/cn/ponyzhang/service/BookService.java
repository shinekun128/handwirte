package cn.ponyzhang.service;

import cn.spring.Autowired;
import cn.spring.Component;
import cn.spring.Scope;

@Component("bookService")
@Scope("prototype")
public class BookService {

    @Autowired
    UserService userService;

    public void test(){
        System.out.println(userService);
    }
}
