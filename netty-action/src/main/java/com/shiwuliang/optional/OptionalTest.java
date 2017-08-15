package com.shiwuliang.optional;

import java.util.Optional;
import java.util.function.Function;

/**
 * OptionalTest
 *
 * @author ziyuan
 * @since 2017-08-15
 */
public class OptionalTest {

    private static class User {

        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static void main(String[] args) {
        User u = null;

        Optional<User> userOpt = Optional.ofNullable(u);
        Optional<String> phOpt1 = userOpt.flatMap((usr) -> Optional.ofNullable(usr.getPhone()));

        //为什么user为null，但是user.getPhone不会报错呢，因为这个时候的userOpt其实已经是一个空optional了
        Optional<String> phOpt2 = userOpt.flatMap(new Function<User, Optional<String>>() {
            @Override
            public Optional<String> apply(User user) {
                return Optional.ofNullable(user.getPhone());
            }
        });

        //这两种都不会出错
        System.out.println(phOpt1.orElse("0000"));
        System.out.println(phOpt2.orElse("0000"));
    }
}
