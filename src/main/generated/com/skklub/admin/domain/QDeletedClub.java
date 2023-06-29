package com.skklub.admin.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDeletedClub is a Querydsl query type for DeletedClub
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeletedClub extends EntityPathBase<DeletedClub> {

    private static final long serialVersionUID = 1744235935L;

    public static final QDeletedClub deletedClub = new QDeletedClub("deletedClub");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath activityDescription = createString("activityDescription");

    public final StringPath belongs = createString("belongs");

    public final StringPath briefActivityDescription = createString("briefActivityDescription");

    public final EnumPath<com.skklub.admin.domain.enums.Campus> campus = createEnum("campus", com.skklub.admin.domain.enums.Campus.class);

    public final StringPath clubDescription = createString("clubDescription");

    public final EnumPath<com.skklub.admin.domain.enums.ClubType> clubType = createEnum("clubType", com.skklub.admin.domain.enums.ClubType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Integer> establishAt = createNumber("establishAt", Integer.class);

    public final StringPath headLine = createString("headLine");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedAt = _super.lastModifiedAt;

    //inherited
    public final StringPath lastModifiedBy = _super.lastModifiedBy;

    public final NumberPath<Long> logoId = createNumber("logoId", Long.class);

    public final StringPath mandatoryActivatePeriod = createString("mandatoryActivatePeriod");

    public final NumberPath<Integer> memberAmount = createNumber("memberAmount", Integer.class);

    public final StringPath name = createString("name");

    public final NumberPath<Long> recruitId = createNumber("recruitId", Long.class);

    public final StringPath regularMeetingTime = createString("regularMeetingTime");

    public final StringPath roomLocation = createString("roomLocation");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath webLink1 = createString("webLink1");

    public final StringPath webLink2 = createString("webLink2");

    public QDeletedClub(String variable) {
        super(DeletedClub.class, forVariable(variable));
    }

    public QDeletedClub(Path<? extends DeletedClub> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDeletedClub(PathMetadata metadata) {
        super(DeletedClub.class, metadata);
    }

}

