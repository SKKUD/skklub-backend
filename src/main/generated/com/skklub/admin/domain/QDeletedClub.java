package com.skklub.admin.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDeletedClub is a Querydsl query type for DeletedClub
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeletedClub extends EntityPathBase<DeletedClub> {

    private static final long serialVersionUID = 1744235935L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDeletedClub deletedClub = new QDeletedClub("deletedClub");

    public final StringPath activityDescription = createString("activityDescription");

    public final ListPath<ActivityImage, QActivityImage> activityImages = this.<ActivityImage, QActivityImage>createList("activityImages", ActivityImage.class, QActivityImage.class, PathInits.DIRECT2);

    public final BooleanPath alive = createBoolean("alive");

    public final StringPath belongs = createString("belongs");

    public final StringPath briefActivityDescription = createString("briefActivityDescription");

    public final EnumPath<com.skklub.admin.domain.enums.Campus> campus = createEnum("campus", com.skklub.admin.domain.enums.Campus.class);

    public final StringPath clubDescription = createString("clubDescription");

    public final EnumPath<com.skklub.admin.domain.enums.ClubType> clubType = createEnum("clubType", com.skklub.admin.domain.enums.ClubType.class);

    public final NumberPath<Integer> establishAt = createNumber("establishAt", Integer.class);

    public final StringPath headLine = createString("headLine");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLogo logo;

    public final StringPath mandatoryActivatePeriod = createString("mandatoryActivatePeriod");

    public final NumberPath<Integer> memberAmount = createNumber("memberAmount", Integer.class);

    public final StringPath name = createString("name");

    public final QUser president;

    public final StringPath regularMeetingTime = createString("regularMeetingTime");

    public final StringPath roomLocation = createString("roomLocation");

    public final StringPath webLink1 = createString("webLink1");

    public final StringPath webLink2 = createString("webLink2");

    public QDeletedClub(String variable) {
        this(DeletedClub.class, forVariable(variable), INITS);
    }

    public QDeletedClub(Path<? extends DeletedClub> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDeletedClub(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDeletedClub(PathMetadata metadata, PathInits inits) {
        this(DeletedClub.class, metadata, inits);
    }

    public QDeletedClub(Class<? extends DeletedClub> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.logo = inits.isInitialized("logo") ? new QLogo(forProperty("logo")) : null;
        this.president = inits.isInitialized("president") ? new QUser(forProperty("president")) : null;
    }

}

