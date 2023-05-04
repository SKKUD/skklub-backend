package com.skklub.admin.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //ㅋㅋ
    private String externId;
    private String password;

    //Integer인게 굉장히 마음에 안듭니다.. 꼭 고쳐주세요
    private Integer authority;

    private String name;
    private String contact;

    public User(String externId, String password, Integer authority, String name, String contact) {
        this.externId = externId;
        this.password = password;
        this.authority = authority;
        this.name = name;
        this.contact = contact;
    }
}
