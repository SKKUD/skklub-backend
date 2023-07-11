package com.skklub.admin.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QExtraFile is a Querydsl query type for ExtraFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExtraFile extends EntityPathBase<ExtraFile> {

    private static final long serialVersionUID = -903259428L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QExtraFile extraFile = new QExtraFile("extraFile");

    public final QBaseTimeEntity _super = new QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedAt = _super.lastModifiedAt;

    public final QNotice notice;

    public final StringPath originalName = createString("originalName");

    public final StringPath savedName = createString("savedName");

    public QExtraFile(String variable) {
        this(ExtraFile.class, forVariable(variable), INITS);
    }

    public QExtraFile(Path<? extends ExtraFile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QExtraFile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QExtraFile(PathMetadata metadata, PathInits inits) {
        this(ExtraFile.class, metadata, inits);
    }

    public QExtraFile(Class<? extends ExtraFile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.notice = inits.isInitialized("notice") ? new QNotice(forProperty("notice"), inits.get("notice")) : null;
    }

}

