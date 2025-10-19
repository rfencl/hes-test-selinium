package com.qa.pojo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.qa.util.JsonSerializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = RecordPojo.RecordPojoBuilder.class)
public class RecordPojo implements JsonSerializable<RecordPojo> {
  private int id;
  private String firstName;
  private String lastName;
  private String dob;
  private String gender;
  private String telephone;
  private int householdId;
  private String hispanic;
  private String hispanicOther;
  private String race;
  private String raceOther;
  private String otherStay;


  @JsonPOJOBuilder(withPrefix = "")
  public static class RecordPojoBuilder {
  }
}
