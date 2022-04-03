-- create database "dbname you like"
-- with encoding  = 'UTF8'
-- lc_collate = 'en_US.UTF8'



create extension if not exists ltree;

create table "Department"
(
    "departmentId" serial  not null
        constraint department_pk
            primary key,
    name           varchar not null
);

alter table "Department"
    owner to postgres;

create unique index department_departmentid_uindex
    on "Department" ("departmentId");

create unique index department_name_uindex
    on "Department" (name);

create table "Course"
(
    "courseId"   varchar not null
        constraint course_pk
            primary key,
    "courseName" varchar,
    credit       integer,
    "classHour"  integer,
    grading      varchar
);

alter table "Course"
    owner to postgres;

create table "User"
(
    id         integer not null
        constraint user_pk
            primary key,
    "fullName" varchar
);

alter table "User"
    owner to postgres;

create table "Instructor"
(
    "userId"    integer not null
        constraint instructor_pk
            primary key
        constraint instructor_user_id_fk
            references "User"
            on delete cascade,
    "firstName" varchar,
    "lastName"  varchar
);

alter table "Instructor"
    owner to postgres;

create table "Major"
(
    id             serial not null
        constraint major_pk
            primary key,
    name           varchar,
    "departmentId" integer
        constraint major_department_departmentid_fk
            references "Department"
            on delete cascade
);

alter table "Major"
    owner to postgres;

create table "Student"
(
    "enrolledDate" date,
    "majorId"      integer
        constraint student_major_id_fk
            references "Major"
            on delete cascade,
    "userId"       integer not null
        constraint student_pk
            primary key
        constraint student_user_id_fk
            references "User"
            on delete cascade,
    "firstName"    varchar,
    "lastName"     varchar
);

alter table "Student"
    owner to postgres;

create table "Semester"
(
    id    serial not null
        constraint semester_pk
            primary key,
    name  varchar,
    begin date,
    "end" date
);

alter table "Semester"
    owner to postgres;

create table "CourseSection"
(
    "sectionId"     serial not null
        constraint coursesection_pk
            primary key,
    "totalCapacity" integer,
    "leftCapacity"  integer,
    "courseId"      varchar
        constraint coursesection_course_courseid_fk
            references "Course"
            on delete cascade,
    "semesterId"    integer
        constraint coursesection_semester_id_fk
            references "Semester"
            on delete cascade,
    "sectionName"   varchar
);

alter table "CourseSection"
    owner to postgres;

create table "CourseSectionClass"
(
    id           serial not null
        constraint coursesectionclass_pk
            primary key,
    instructor   integer
        constraint coursesectionclass_instructor_userid_fk
            references "Instructor"
            on delete cascade,
    "dayOfWeek"  varchar,
    location     varchar,
    "weekList"   smallint[],
    "sectionId"  integer
        constraint coursesectionclass_coursesection_sectionid_fk_2
            references "CourseSection"
            on delete cascade,
    "classStart" smallint,
    "classEnd"   smallint
);

alter table "CourseSectionClass"
    owner to postgres;

create table "Major_Course"
(
    "majorId"  integer
        constraint major_course_major_id_fk
            references "Major"
            on delete cascade,
    "courseId" varchar
        constraint major_course_course_courseid_fk
            references "Course"
            on delete cascade,
    property   varchar,
    id         serial not null
        constraint major_course_pk
            primary key
);

alter table "Major_Course"
    owner to postgres;

create unique index major_course_majorid_uindex
    on "Major_Course" ("majorId", "courseId");


--alter table prerequisite
    --owner to postgres;

create table student_section
(
    "studentId" integer
        constraint student_section_student_userid_fk
            references "Student"
            on delete cascade,
    "sectionId" integer
        constraint student_section_coursesection_sectionid_fk
            references "CourseSection"
            on delete cascade,
    grade       varchar
);

alter table student_section
    owner to postgres;



create or replace function add_Department(nameIn varchar) returns integer
    language plpgsql
as
$$
BEGIN
    insert into "Department" (name) values (nameIn);
    RETURN (select "departmentId" from "Department" where name = nameIn);
END
$$;



CREATE OR REPLACE FUNCTION remove_Department(departmentId int)
    returns void
as
$$
BEGIN
    IF ((select count(*) from "Department" where "departmentId" = departmentId) = 0) THEN
        RAISE EXCEPTION '';
    end if;
    DELETE
    FROM "Department"
    WHERE "departmentId" = departmentId;
END
$$
    LANGUAGE plpgsql;


CREATE OR REPLACE function getAllDepartments()
    RETURNS TABLE
            (
                departmentId_out integer,
                name_out         varchar
            )
AS
$$
BEGIN
    RETURN QUERY
        select "departmentId" as departmentId_out, "Department".name as name_out from "Department";
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function getDepartment(department int)
    RETURNS TABLE
            (
                departmentId_out integer,
                name_out         varchar
            )
AS
$$
BEGIN
    IF ((select count(*) from "Department" where "departmentId" = department) = 0) THEN
        RAISE EXCEPTION '';
    END IF;
    RETURN QUERY
        select "departmentId", "Department".name from "Department" where "departmentId" = department;
END
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE function add_Major(nameIn varchar, departmentId int)
    RETURNS integer
AS
$$
BEGIN
    insert into "Major" (name, "departmentId") VALUES (nameIn, departmentId);
    RETURN (select id from "Major" where name = nameIn and "departmentId" = departmentId);
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION remove_Major(majorId int)
    RETURNS VOID
    language plpgsql
AS
$$
BEGIN
    if ((select count(*) from "Major" where id = majorId) = 0) then
        raise exception '' ;
    end if;

    DELETE
    FROM "Major"
    WHERE id = majorId;
end
$$;



CREATE OR REPLACE function get_all_Majors()
    RETURNS TABLE
            (
                id              integer,
                major_name      varchar,
                department_id   integer,
                department_name varchar
            )
AS
$$
BEGIN
    RETURN QUERY
        select "Major".id,
               "Major".name           as Major_name,
               "Major"."departmentId" as department_Id,
               "Department".name      as Department_Name
        from "Major"
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId";
END
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE function get_Major(majorId int)
    RETURNS table
            (
                id              integer,
                major_name      varchar,
                department_id   integer,
                department_name varchar
            )
AS
$$
BEGIN
    if ((select count(*) from "Major" where id = majorId) = 0) then
        RAISE EXCEPTION '';
    end if;
    RETURN QUERY
        select "Major".id,
               "Major".name           as Major_name,
               "Major"."departmentId" as department_Id,
               "Department".name      as Department_Name
        from "Major"
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId"
        where "Major".id = majorId;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function add_Major_Compulsory_Course(majorId int, courseId varchar) --Compulsory
    returns void
    LANGUAGE SQL
as
$$
insert into "Major_Course" ("majorId", "courseId", property)
VALUES (majorId, courseId, 'MAJOR_COMPULSORY');
$$;



CREATE OR REPLACE FUNCTION add_Major_Elective_Course(majorId int, courseId varchar) --Elective
    returns void
    LANGUAGE SQL
as
$$
insert into "Major_Course" ("majorId", "courseId", property)
VALUES (majorId, courseId, 'MAJOR_ELECTIVE');
$$;


CREATE OR REPLACE FUNCTION remove_Semester(semesterId int)
    returns void
    LANGUAGE plpgsql
as
$$
begin
    if ((select count(*) from "Semester" where id = semesterId) = 0) then
        raise exception '';
    end if;

    DELETE
    FROM "Semester"
    WHERE id = semesterId;
end
$$;

CREATE OR REPLACE function add_Semester(nameIn varchar, beginIn date, end_In date)
    RETURNS integer
AS
$$
BEGIN
    insert into "Semester" (name, begin, "end") VALUES (nameIn, beginIn, end_In);
    RETURN (select id from "Semester" where name = nameIn and begin = beginIn and "end" = end_In);
END
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE function get_all_Semester()
    RETURNS TABLE
            (
                id_out        integer,
                semester_name varchar,
                begin_        date,
                end_          date
            )
AS
$$
BEGIN
    RETURN QUERY
        select "Semester".id as id_out, name as semester_name, begin as begin_, "end" as end_
        from "Semester";
END
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE function get_Semester(semesterId int)
    RETURNS TABLE
            (
                id_out        integer,
                semester_name varchar,
                begin_        date,
                end_          date
            )
AS
$$
BEGIN
    if ((select count(*) from "Semester" where "Semester".id = semesterId) = 0) then
        raise exception '';
    end if;
    RETURN QUERY
        select "Semester".id, name, begin, "end"
        from "Semester"
        where "Semester".id = semesterId;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION remove_User(userId int)
    RETURNS VOID
    language plpgsql
AS
$$
BEGIN
    if ((select count(*) from "User" where id = userId) = 0) then
        raise exception '' ;
    end if;

    DELETE
    FROM "User"
    WHERE id = userId;
end
$$;



create or replace function get_all_users()
    returns TABLE
            (
                userId          integer,
                fullName        varchar,
                enrolledDate    date,
                department_name varchar,
                MajorId         integer,
                major_name      varchar,
                department_id   integer
            )
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        select "User".id, "fullName", "enrolledDate", department_name, "majorId", major_name, "Major"."departmentId"
        from "User"
                 left join "Student" on "User".id = "Student"."userId"
                 left join "Instructor" on "User".id = "Instructor"."userId"
                 left join "Major" on "Student"."majorId" = "Major".id
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId";
END
$$;


create or replace function get_User(userIdIn integer)
    returns TABLE
            (
                userId          integer,
                fullName        varchar,
                enrolledDate    date,
                department_name varchar,
                MajorId         integer,
                major_name      varchar,
                department_id   integer
            )
    language plpgsql
as
$$
BEGIN
    if ((select count(*) from "User" where id = userIdIn) = 0) then
        raise exception '';
    end if;
    RETURN QUERY
        select "User".id, "fullName", "enrolledDate", "Department".name, "majorId", "Major".name, "Major"."departmentId"
        from "User"
                 left join "Student" on "User".id = "Student"."userId"
                 left join "Instructor" on "User".id = "Instructor"."userId"
                 left join "Major" on "Student"."majorId" = "Major".id
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId"
        where "User".id = userIdIn;
END
$$;


CREATE OR REPLACE function add_Instructor(userId int, firstName varchar, lastName varchar, fullname_in varchar)
    RETURNS integer
AS
$$
BEGIN
            insert into "User" (id, "fullName") VALUES (userId, fullname_in);
    insert into "Instructor" ("userId", "firstName", "lastName") VALUES (userId, firstName, lastName);
    RETURN userId;
END
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE function get_Instructed_CourseSections(instructorIdIn int, semesterIdIn int)
    RETURNS TABLE
            (
                courseSectionIdOut   int,
                courseSectionNameOut varchar,
                leftCapacityOut      int,
                totalCapacityOut     int
            )
AS
$$
BEGIN

    if ((select count(*) from "Instructor" where "userId" = instructorIdIn) = 0) then raise exception ''; end if;
    if ((select count(*) from "Semester" where id = semesterIdIn) = 0) then raise exception ''; end if;

    RETURN QUERY
        select "CourseSection"."sectionId", "sectionName", "leftCapacity", "totalCapacity"
        from "CourseSectionClass"
                 left join "CourseSection" on "CourseSectionClass"."sectionId" = "CourseSection"."sectionId"
        where instructor = instructorIdIn
          and "semesterId" = semesterIdIn;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function add_Course(courseId varchar, courseName varchar, credit int, classHour int, grading varchar)
    returns void
AS
$$
BEGIN
    insert into "Course" ("courseId", "courseName", credit, "classHour", grading)
    VALUES (courseId, courseName, credit, classHour, grading);
END;
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function remove_Coures(courseId varchar)
    returns void
AS
$$
BEGIN
    if ((select count(*) from "Course" where "courseId" = courseId) = 0) then raise exception ''; end if;

    delete from "Course" where "courseId" = courseId;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function remove_Coures_Section(sectionId int)
    returns void
AS
$$
BEGIN
    if ((select count(*) from "CourseSection" where "sectionId" = sectionId) = 0) then raise exception ''; end if;

    delete from "CourseSection" where "sectionId" = sectionId;
END
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE function remove_Coures_Section_Class(classId int)
    returns void
AS
$$
BEGIN
    if ((select count(*) from "CourseSectionClass" where id = classId) = 0) then raise exception ''; end if;

    delete from "CourseSectionClass" where id = classId;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function get_Course_Sections_In_Semester(courseId varchar, semesterId int)
    RETURNS TABLE
            (
                idOut         integer,
                nameOut       varchar,
                leftCapacity  integer,
                totalCapacity integer
            )
AS
$$
BEGIN

    if ((select count(*) from "Course" where "courseId" = courseId) = 0) then raise exception ''; end if;

    if ((select count(*) from "Semester" where id = semesterId) = 0) then raise exception ''; end if;

    RETURN QUERY
        select "sectionId", "sectionName", "leftCapacity", "totalCapacity"
        from "CourseSection"
        where "courseId" = courseId
          and "semesterId" = semesterId;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function get_Course_By_Section(sectionId int)
    RETURNS TABLE
            (
                id         varchar,
                name       varchar,
                creditOut  integer,
                classHour  integer,
                gradingOut varchar
            )
AS
$$
BEGIN

    if ((select count(*) from "CourseSection" where "sectionId" = sectionId) = 0) then raise exception ''; end if;

    RETURN QUERY
        select "Course"."courseId", "courseName", credit, "classHour", grading
        from "CourseSection"
                 left join "Course" on "CourseSection"."courseId" = "Course"."courseId"
        where "sectionId" = sectionId;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function get_Course_Section_Classes(sectionIdIn int)
    RETURNS TABLE
            (
                idOut               int,
                dayOfWeek           varchar,
                weekList            smallint[],
                classBegin          smallint,
                classEnd            smallint,
                locationOut         varchar,
                instructor_fullName varchar,
                instructor_idOut    integer
            )
AS
$$
BEGIN
    if ((select count(*) from "CourseSection" where "sectionId" = sectionIdIn) = 0) then raise exception ''; end if;

    RETURN QUERY
        select "CourseSectionClass".id,
               "dayOfWeek",
               "weekList",
               "classStart",
               "classEnd",
               location,
               "fullName",
               instructor
        from "CourseSectionClass"
                 left join "Instructor" on "CourseSectionClass".instructor = "Instructor"."userId"
                 left join "User" on "Instructor"."userId" = "User".id
        where "CourseSectionClass"."sectionId" = sectionIdIn;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE function get_Course_Section_By_Class(classId int)
    RETURNS TABLE
            (
                sectionId     integer,
                nameOut       varchar,
                leftCapacity  integer,
                totalCapacity integer
            )
AS
$$
BEGIN

    if ((select count(*) from "CourseSectionClass" where id = classId) = 0) then raise exception ''; end if;

    RETURN QUERY
        select "CourseSection"."sectionId", "sectionName", "leftCapacity", "totalCapacity"
        from "CourseSectionClass"
                 left join "CourseSection" on "CourseSectionClass"."sectionId" = "CourseSection"."sectionId"
        where id = classId;
END
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION remove_course_trigger_function()
    returns trigger
as
$$
declare
begin
    update "CourseSection" set "leftCapacity" = "leftCapacity" + 1 where "sectionId" = old."sectionId";
    return new;
end

$$ language plpgsql;

CREATE TRIGGER remove_course_trigger
    AFTER delete
    on student_section
    for each row
execute procedure remove_course_trigger_function();



CREATE OR REPLACE function get_Enrolled_Students_In_Semester(courseId varchar, semesterId int)
    returns TABLE
            (
                studentId         integer,
                fullNameOut       varchar,
                enrolledDateOut   date,
                majorIdOut        int,
                majorNameOut      varchar,
                departmentIdOut   integer,
                departmentNameOut varchar
            )
AS
$$
BEGIN
    if ((select count(*) from "Course" where "courseId" = courseId) = 0) then raise exception ''; end if;

    if ((select count(*) from "Semester" where id = semesterId) = 0) then raise exception ''; end if;

    return query
        select "userId",
               "fullName",
               "enrolledDate",
               "majorId",
               "Major".name,
               "Department"."departmentId",
               "Department".name
        from "Student"
                 left join "User" on "Student"."userId" = "User".id
                 left join "Major" on "Student"."majorId" = "Major".id
                 left join "Department" on "Major"."departmentId" = "Department"."departmentId";
END
$$ LANGUAGE 'plpgsql';



create index name1 on student_section("studentId");
create index name2 on student_section("sectionId");




create function add_course(courseid character varying, coursename character varying, credit integer, classhour integer, grading character varying) returns void
    language plpgsql
as
$$
BEGIN
    insert into "Course" ("courseId", "courseName", credit, "classHour", grading)
    VALUES (courseId, courseName, credit, classHour, grading);
END;
$$;

alter function add_course(varchar, varchar, integer, integer, varchar) owner to postgres;


create function add_coursesection(courseid character varying, semesterid integer, sectionname character varying, totalcapacity integer, leftcapacity integer) returns integer
    language plpgsql
as
$$
BEGIN
    if (courseId not in (select "courseId" from "Course" WHERE "courseId" = courseId))
    then
        insert into "Course"("courseId") values (courseId);
        insert into "CourseSection" ("courseId", "semesterId", "sectionName", "totalCapacity", "leftCapacity")
        VALUES (courseId, semesterId, sectionName, totalCapacity, leftCapacity);
        return (select max("sectionId") from "CourseSection");

    else
        insert into "CourseSection" ("courseId", "semesterId", "sectionName", "totalCapacity", "leftCapacity")
        VALUES (courseId, semesterId, sectionName, totalCapacity, leftCapacity);
        return (select max("sectionId") from "CourseSection");

    end if;
end ;

$$;

alter function add_coursesection(varchar, integer, varchar, integer, integer) owner to postgres;

create function add_coursesectionclass(sectionid integer, instructorid integer, dayofweek character varying, weeklist smallint[], classstart smallint, classend smallint, location1 character varying) returns integer
    language plpgsql
as
$$
BEGIN

    if (sectionId not in (SELECT "sectionId" from "CourseSection" WHERE "sectionId" = sectionId))
    then
        insert into "CourseSection"("sectionId") values (sectionId);
        insert into "CourseSectionClass" ("sectionId", "instructor", "dayOfWeek", "weekList", "classStart", "classEnd",
                                          "location")
        VALUES (sectionId, instructorId, dayOfWeek, weekList, classStart, classEnd, location1);
        return (select max("id") from "CourseSectionClass");
    else
        insert into "CourseSectionClass" ("sectionId", "instructor", "dayOfWeek", "weekList", "classStart", "classEnd",
                                          "location")
        VALUES (sectionId, instructorId, dayOfWeek, weekList, classStart, classEnd, location1);
        return (select max("id") from "CourseSectionClass");
    end if;

end;

$$;

alter function add_coursesectionclass(integer, integer, varchar, smallint[], smallint, smallint, varchar) owner to postgres;

create function dropcourse(studentid integer, sectionid integer) returns void
    language plpgsql
as
$$
BEGIN
    if ((select count(*) from "student_section" where "studentId" = studentId) = 0) then raise exception ''; end if;


    delete from "student_section" where "studentId" = studentId and "sectionId" = sectionId;
END
$$;

alter function dropcourse(integer, integer) owner to postgres;

create function get_conflictcoursenames(studentid integer, sectionid integer)
    returns TABLE(coursename character varying, sectionname character varying)
    language plpgsql
as
$$
declare

BEGIN


    RETURN QUERY
        select get_course_time_conflict.coursename, get_course_time_conflict.sectionname
         from get_course_time_conflict(studentId, sectionid)
         union
         select get_course_conflict.coursename, get_course_conflict.sectionname
         from get_course_conflict(studentId, sectionid);


END
$$;

alter function get_conflictcoursenames(integer, integer) owner to postgres;

create function get_course_conflict(studentid integer, sectionidinfunction integer)
    returns TABLE(coursename character varying, sectionname character varying)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        with T1 as (select "courseId"
                    from student_section
                             join "CourseSection" CS on student_section."sectionId" = CS."sectionId"
                    where "studentId" = studentId
                      and CS."sectionId" = sectionIdInFunction)
        select C."courseName",CS2."sectionName"
        from student_section
                 join "CourseSection" CS2 on student_section."sectionId" = CS2."sectionId"
                 join T1 on T1."courseId" = CS2."courseId"
                 join "Course" C on CS2."courseId" = C."courseId"
        where "studentId" = studentId
          and CS2."sectionId" != sectionIdInFunction;

END
$$;

alter function get_course_conflict(integer, integer) owner to postgres;

create function get_course_prerequisite(studentid integer, sectionid_f integer)
    returns TABLE(courseid character varying)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        select courseId
        from student_section ss
                 join "CourseSection" cs on ss."sectionId" = cs."sectionId"
        where "studentId" = studentId
          and ss."sectionId" = sectionId_F;
END
$$;

alter function get_course_prerequisite(integer, integer) owner to postgres;

create function get_course_time_conflict(studentid integer, sectionidinfunction integer)
    returns TABLE(coursename character varying, sectionname character varying)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        with T1 as (select C."courseName",
                           "studentId",
                           ss1."sectionId",
                           grade,
                           csc1."dayOfWeek",
                           "classStart",
                           "classEnd",
                           "weekList",
                           CS."sectionName"
                    from student_section as ss1
                             join "CourseSectionClass" as csc1 on ss1."sectionId" = csc1."sectionId"
                             join "CourseSection" CS on CS."sectionId" = csc1."sectionId"
                             join "Course" C on C."courseId" = CS."courseId"
                    where grade is null
                      and "studentId" = studentId
                      and ss1."sectionId" = sectionIdInFunction)

        select T1."courseName", T1."sectionName"
        from "CourseSectionClass" as cour
                 join student_section ss on cour."sectionId" = ss."sectionId"
                 join T1 on T1."sectionId" = ss."sectionId"
        where ss."studentId" = T1."studentId"
          and ss.grade = T1.grade
          and T1."sectionId" != cour."sectionId"
          and T1."dayOfWeek" = cour."dayOfWeek"
          and T1."classStart" = cour."classStart"
          and T1."classEnd" = cour."classEnd"
          and ((select unnest(T1."weekList")
                intersect
                select unnest(cour."weekList")) is not null);


END
$$;

alter function get_course_time_conflict(integer, integer) owner to postgres;

create function get_coursetable(studentid integer, semesterid integer)
    returns TABLE(coursename character varying, sectionname character varying, userid integer, firstname character varying, lastname character varying, location character varying, classstart smallint, classend smallint, dayofweek character varying)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        SELECT cs."sectionName", cour."courseName", I."userId", I."firstName", I."lastName", csc.location, csc."classStart", csc."classEnd", csc."dayOfWeek"
        FROM "CourseSection" cs
                 join "student_section" as ss on cs."sectionId" = ss."sectionId"
                 join "Course" cour on cour."courseId" = cs."courseId"
                 join "CourseSectionClass" csc on csc."sectionId" = ss."sectionId"
                 join "Instructor" I on csc.instructor = I."userId"
        where ss."studentId" = studentId
          and cs."semesterId" = semesterId;
END
$$;

alter function get_coursetable(integer, integer) owner to postgres;

create function get_weeklist(studentid integer, sectionidinfunction integer)
    returns TABLE(weeklist smallint[])
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        --首先找出选择该门课程的weeklist
        select weekList from "CourseSectionClass" join student_section ss on "CourseSectionClass"."sectionId" = ss."sectionId"
    where "studentId" = studentId and ss."sectionId" = sectionidinfunction;
END
$$;

alter function get_weeklist(integer, integer) owner to postgres;

create function getalldepartments()
    returns TABLE(departmentid_out integer, name_out character varying)
    language plpgsql
as
$$
BEGIN
    RETURN QUERY
        select "departmentId" as departmentId_out, "Department".name as name_out from "Department";
END
$$;

alter function getalldepartments() owner to postgres;

create function remove_department(departmentid integer) returns void
    language plpgsql
as
$$
BEGIN
    IF ((select count(*) from "Department" where "departmentId" = departmentId) = 0) THEN
        RAISE EXCEPTION '';
    end if;
    DELETE
    FROM "Department"
    WHERE "departmentId" = departmentId;
END
$$;

alter function remove_department(integer) owner to postgres;

create function search_course(studentid integer, semesterid integer, searchcid character varying, searchname character varying, searchinstructor character varying, searchdayofweek character varying, searchclasstime smallint, searchclasslocations character varying[], searchcoursetype character varying, ignorefull boolean, ignoreconflict boolean, ignorepassed boolean, ignoremissingprerequisites boolean, pagesize integer, pageindex integer)
    returns TABLE(cid1 character varying, name1 character varying, credit1 integer, classhour1 integer, grading1 character varying, sectionid1 integer, sectionname1 character varying, totalcapacity1 integer, leftcapacity1 integer, classid1 integer, instructorid1 integer, firstname1 character varying, lastname1 character varying, dayofweek1 character varying, weeklist1 smallint[], classbegin1 smallint, classend1 smallint, location1 character varying, grade1 character varying)
    language plpgsql
as
$$
declare
    bo_searchCid            boolean;
    bo_searchName           boolean;
    bo_searchInstructor     boolean;
    bo_searchDayOfWeek      boolean;
    bo_searchClassTime      boolean;
    bo_searchClassLocations boolean;


BEGIN
    if (searchCid is not null) then
        bo_searchCid = true ;
    end if;
    if (searchName is not null) then
        bo_searchName = true;
    end if;
    if (bo_searchInstructor is not null) then
        bo_searchInstructor = true;
    end if;
    if (bo_searchDayOfWeek is not null) then
        bo_searchDayOfWeek = true;
    end if;
    if (bo_searchClassTime is not null) then
        bo_searchClassTime = true;
    end if;
    if (bo_searchClassLocations is not null) then
        bo_searchClassLocations = true;
    end if;


    RETURN QUERY
        select "Course"."courseId",
               "Course"."courseName",
               "Course".credit,
               "classHour",
               "Course".grading,
               CS."sectionId",
               "sectionName",
               "totalCapacity",
               "leftCapacity",
               csc.id,
               instructor,
               "firstName",
               "lastName",
               "dayOfWeek",
               "weekList",
               "classStart",
               "classEnd",
               CSC.location,
               ss.grade
        from "Course"
                 left join "CourseSection" CS
                           on "Course"."courseId" = CS."courseId" and CS."semesterId" = semesterId
                 right join "CourseSectionClass" CSC on CS."sectionId" = CSC."sectionId"
                 left join student_section ss on CS."sectionId" = ss."sectionId"
                 left join "Instructor" I on CSC.instructor = I."userId"
                 left join "Major_Course" MC on "Course"."courseId" = MC."courseId"

        where ss."studentId" = studentId
          and case
                  when (bo_searchCid) then searchCid = CS."courseId" end
          and case
                  when (bo_searchName) then
                      searchName = ("courseName" || '[' || "sectionName" || ']') end
          and case
                  when (bo_searchInstructor) then
                      ("firstName" LIKE (searchInstructor || '%') OR
                       "lastName" LIKE
                       (searchInstructor || '%') OR
                       ("firstName" ||
                        "lastName") LIKE
                       (searchInstructor || '%') OR
                       ("firstName" || ' ' ||
                        "lastName") LIKE (searchInstructor || '%')
                          ) end
          and case
                  when (bo_searchDayOfWeek) then
                      ("dayOfWeek" = searchDayOfWeek) end
          and case
                  when (bo_searchClassTime)
                      then
                      searchclasstime = "classHour" end
          and case
                  when (bo_searchClassLocations)
                      then
                      location = any (searchClassLocations) end
          and (case
                   when (searchCourseType is not null and property is not null)
                       then
                       (searchCourseType = property) end
            or searchCourseType = 'ALL')
          and ((not ignoreFull) or "leftCapacity" > 0)
          and (not ignorePassed or
               ((grading = 'PASS_OR_FAIL' and grade != 'PASS') or
                (grading = 'HUNDRED_MARK_SCORE' and cast(grade as integer) < 60)))
          and ((not ignoreMissingPrerequisites)
            or (select passed_prerequisites_for_course(studentId, CS."courseId", null, null)))
          and ((not ignoreConflict)
            or ((select count(1) from get_course_conflict(studentId, CSC."sectionId")) = 0 and
                (select count(1)
                 from get_course_time_conflict(studentId, CSC."sectionId")) =
                0))
        order by "courseId", "courseName" || '[' || CS."sectionName" || ']'
        limit pageSize offset pageIndex;


END
$$;

alter function search_course(integer, integer, varchar, varchar, varchar, varchar, smallint, character varying[], varchar, boolean, boolean, boolean, boolean, integer, integer) owner to postgres;

