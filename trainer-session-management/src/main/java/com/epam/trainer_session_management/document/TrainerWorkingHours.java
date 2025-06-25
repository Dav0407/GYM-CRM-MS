package com.epam.trainer_session_management.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "first_last_name_idx", def = "{'trainerFirstName' : 1, 'trainerLastName': 1}")
})
@Document(collection = "trainer_working_hours")
public class TrainerWorkingHours {

    @Id
    private String trainerUsername;

    @Field(targetType = FieldType.STRING)
    private String trainerFirstName;

    @Field(targetType = FieldType.STRING)
    private String trainerLastName;

    @Field(targetType = FieldType.BOOLEAN)
    private Boolean isActive;

    @Field(targetType = FieldType.ARRAY)
    private List<Year> years;

    @Getter
    @Setter
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Year{

        @Field(targetType = FieldType.STRING)
        private String year;

        @Field(targetType = FieldType.ARRAY)
        private List<Month> months;
    }

    @Getter
    @Setter
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Month{

        @Field(targetType = FieldType.STRING)
        private String month;

        @Field(targetType = FieldType.DOUBLE)
        private Float monthlyWorkingHours;

        @Field(targetType = FieldType.ARRAY)
        private List<Day> days;
    }

    @Getter
    @Setter
    @ToString
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Day{

        @Field(targetType = FieldType.STRING)
        private String day;

        @Field(targetType = FieldType.DOUBLE)
        private Float dailyWorkingHours;
    }
}
