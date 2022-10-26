/**
 *     MyPay - Payment portal of Regione Veneto.
 *     Copyright (C) 2022  Regione Veneto
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.regioneveneto.mygov.payment.mypay4.util.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.*;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldBean implements Serializable {

  private static final long serialVersionUID = 825096468975807094L;

  @SerializedName("name")
  private String name;

  @SerializedName("required")
  private boolean required;

  @SerializedName("regex")
  private String regex;

  @SerializedName("html_render")
  private RenderType htmlRender;

  @SerializedName("html_class")
  private String htmlClass;

  @SerializedName("html_label")
  private String htmlLabel;

  @SerializedName("html_placeholder")
  private String htmlPlaceholder;

  @SerializedName("bind_cms")
  private String bindCms;

  @SerializedName("default_value")
  private String defaultValue;

  @SerializedName("ins_order")
  private int insertableOrder;

  @SerializedName("isIndexable")
  private boolean indexable;

  @SerializedName("renderable_order")
  private int renderableOrder;

  @SerializedName("ser_order")
  private int searchableOrder;

  @SerializedName("lis_order")
  private int listableOrder;

  private boolean isInsertable;

  private boolean isRenderable;

  private boolean isSearchable;

  private boolean isListable;

  private boolean isAssociation;

  private boolean isDetailLink;

  private String associationField;

  @SerializedName("min_occurences")
  private int minOccurences;

  @SerializedName("max_occurences")
  private int maxOccurences;

  @SerializedName("group_by")
  private String groupBy;

  @SerializedName("extra_map")
  private Map<String, String> extraAttr;

  @SerializedName("enumeration_list")
  private List<String> enumerationList;

  @SerializedName("subfields")
  private List<FieldBean> subfields;

  @SerializedName("valid_depends_on")
  private String validDependsOn;

  @SerializedName("valid_depends_on_uids")
  private String validDependsOnUids;

  @SerializedName("value_depends_on")
  private String valueDependsOn;

  @SerializedName("value_depends_on_uids")
  private String valueDependsOnUids;

  @SerializedName("hidden_depends_on")
  private String hiddenDependsOn;

  @SerializedName("hidden_depends_on_uids")
  private String hiddenDependsOnUids;

  @SerializedName("mandatory_depends_on")
  private String mandatoryDependsOn;

  @SerializedName("mandatory_depends_on_uids")
  private String mandatoryDependsOnUids;

  @SerializedName("enabled_depends_on")
  private String enabledDependsOn;

  @SerializedName("enabled_depends_on_uids")
  private String enabledDependsOnUids;

  @SerializedName("error_message")
  private String errorMessage;

  @SerializedName("help_message")
  private String helpMessage;

  public String toJson() {
    return new Gson().toJson(this);
  }

  public String toJson(Class<? extends Annotation> ann) {

    if (ann == null) {
      return this.toJson();
    }
    Gson gson = new GsonBuilder().setPrettyPrinting().setExclusionStrategies(new GsonInclusionStrategy(ann))
        .create();
    return gson.toJson(this, new TypeToken<FieldBean>() {
    }.getType());
  }

  @SneakyThrows
  public Map<String, String> getMapOfExtraAttrByKey(String key) {
    if(this.extraAttr.containsKey(key)) {
      var valueMap = this.extraAttr.get(key);
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(valueMap.replaceAll("'","\""), Map.class);
    }
    return Map.of();
  }
}
