package com.skklub.admin.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QExtraFile is a Querydsl query type for ExtraFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QExtraFile extends EntityPathBase<ExtraFile> {

    private static final long serialVersionUID = -903259428L;

    public static final QExtraFile extraFile = new QExtraFile("extraFile");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath originalName = createString("originalName");

    public final StringPath savedName = createString("savedName");

    public QExtraFile(String variable) {
        super(ExtraFile.class, forVariable(variable));
    }

    public QExtraFile(Path<? extends ExtraFile> path) {
        super(path.getType(), path.getMetadata());
    }

    public QExtraFile(PathMetadata metadata) {
        super(ExtraFile.class, metadata);
    }

}

