package org.cloudfoundry.community.servicebroker.brooklyn.service.plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.brooklyn.util.yaml.Yamls;
import org.cloudfoundry.community.servicebroker.brooklyn.config.BrooklynConfig;
import org.cloudfoundry.community.servicebroker.brooklyn.model.DefaultBlueprintPlan;
import org.cloudfoundry.community.servicebroker.brooklyn.service.BrooklynRestAdmin;
import org.cloudfoundry.community.servicebroker.brooklyn.service.ServiceUtil;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SizePlanStrategy extends AbstractCatalogPlanStrategy {

    private final BrooklynConfig brooklynConfig;

    private static final Logger LOG = LoggerFactory.getLogger(SizePlanStrategy.class);

    @Autowired
    public SizePlanStrategy(BrooklynRestAdmin admin, BrooklynConfig brooklynConfig, PlaceholderReplacer replacer) {
    	super(admin, replacer, brooklynConfig);
        this.brooklynConfig = brooklynConfig;
    }

    @Override
    public List<Plan> makePlans(String serviceId, String yaml) {
        List<Plan> plans = new ArrayList<>();
        if (yaml == null) {
            return plans;
        }

        Iterator<Object> iterator = Yamls.parseAll(yaml).iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof Map){
                Map<String, Object> map = (Map<String, Object>) next;
                if (map.containsKey("brooklyn.config")){
                    Map<String, Object> brooklynConfig = (Map<String, Object>)map.get("brooklyn.config");
                    return parseConfig(brooklynConfig, serviceId);
                }
            }
        }
        return plans;
    }

    private List<Plan> parseConfig(Map<String, Object> brooklynConfigMap, String serviceId) {
        Map<String, Object> brokerConfig = (Map<String, Object>)brooklynConfigMap.get("broker.config");
        brokerConfig = replacer().replaceValues(brokerConfig);
        Map<String, Object> planConfig = (Map<String, Object>)brokerConfig.get("plan.config");
        Object plans = brokerConfig.get("plans");
		if (plans instanceof Map) {
            Map<String, Object> planDefinitions = (Map<String, Object>)plans;
            return getPlansFromMap(planDefinitions, serviceId, planConfig);
        } else if (plans instanceof List) {
        	List<Object> planDefinitions = (List<Object>) plans;
        	return getPlansfromList(planDefinitions, serviceId, planConfig);
        } else {
        	LOG.error("Unable to parse config {}", plans);
        	return Collections.emptyList();
        }
    }
    
    private List<Plan> getPlansFromMap(Map<String, Object> planDefinitions, String serviceId, Map<String, Object> planConfig) {
    	List<Plan> plans = new ArrayList<>();
    	Set<String> names = Sets.newHashSet();
        for (String planName : planDefinitions.keySet()) {
            Map<String, Object> planDefinition = (Map<String, Object>) planDefinitions.get(planName);
            Map<String, Object> properties = Maps.newHashMap();
            if(planConfig != null){
            	properties.putAll(planConfig);
            }
            if(planDefinition != null){
            	properties.putAll(planDefinition);
            }
            properties.put("location", brooklynConfig.getLocation());
            String id = serviceId + "." + planName;
            String name = ServiceUtil.getUniqueName(planName, names);
            String description = planName;
            Plan plan = new DefaultBlueprintPlan(id, name, description, properties);
            plans.add(plan);
        }
        return plans;
    }
    
    private List<Plan> getPlansfromList(List<Object> planDefinitions, String serviceId, Map<String, Object> planConfig) {
    	List<Plan> plans = new ArrayList<>();
    	Set<String> names = Sets.newHashSet();
    	for(Object planDefinition : planDefinitions){
    		Map<String, Object> planMap = (Map<String, Object>) planDefinition;
    		String planName = String.valueOf(planMap.get("name"));
    		Map<String, Object> properties = Maps.newHashMap();
    		Map<String, Object> config = (Map<String, Object>) planMap.get("plan.config");
    		if(planConfig != null){
            	properties.putAll(planConfig);
            }
            if(config != null){
            	properties.putAll(config);
            }
            properties.put("location", brooklynConfig.getLocation());
			String id = serviceId + "." + planName;
    		String name = ServiceUtil.getUniqueName(planName, names);
    		String description = String.valueOf(planMap.get("description"));
    		Plan plan = new DefaultBlueprintPlan(id, name, description, properties);
            plans.add(plan);
    	}
    	return plans;
    }

}
