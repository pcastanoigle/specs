package deployment;

import org.junit.Test;

import com.atlassian.bamboo.specs.api.builders.deployment.Deployment;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.exceptions.PropertiesValidationException;
import com.atlassian.bamboo.specs.api.util.EntityPropertiesBuilders;

public class PlanSpecTest {
    @Test
    public void checkYourPlanOffline() throws PropertiesValidationException {
        Plan plan = new PlanSpec().createBuildPlan();
        EntityPropertiesBuilders.build(plan);
        
        Deployment deployment = new PlanSpec().createDeploymentProject();
        EntityPropertiesBuilders.build(deployment);
                
    }
}
