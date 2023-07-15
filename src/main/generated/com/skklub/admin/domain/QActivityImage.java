package com.skklub.admin.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QActivityImage is a Querydsl query type for ActivityImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QActivityImage extends EntityPathBase<ActivityImage> {

    private static final long serialVersionUID = 1408705724L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QActivityImage activityImage = new QActivityImage("activityImage");

    public final QBaseTimeEntity _super = new QBaseTimeEntity(this);

    public final QClub club;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedAt = _super.lastModifiedAt;

    public final StringPath originalName = createString("originalName");

    public final StringPath uploadedName = createString("uploadedName");

    public QActivityImage(String variable) {
        this(ActivityImage.class, forVariable(variable), INITS);
    }

    public QActivityImage(Path<? extends ActivityImage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QActivityImage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QActivityImage(PathMetadata metadata, PathInits inits) {
        this(ActivityImage.class, metadata, inits);
    }

    public QActivityImage(Class<? extends ActivityImage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.club = inits.isInitialized("club") ? new QClub(forProperty("club"), inits.get("club")) : null;
    }

}

