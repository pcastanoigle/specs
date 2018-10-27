package deployment;

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.deployment.Deployment;
import com.atlassian.bamboo.specs.api.builders.deployment.Environment;
import com.atlassian.bamboo.specs.api.builders.deployment.ReleaseNaming;
import com.atlassian.bamboo.specs.api.builders.permission.DeploymentPermissions;
import com.atlassian.bamboo.specs.api.builders.permission.EnvironmentPermissions;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.builders.repository.git.GitRepository;
import com.atlassian.bamboo.specs.builders.task.CleanWorkingDirectoryTask;
import com.atlassian.bamboo.specs.builders.task.ScriptTask;
import com.atlassian.bamboo.specs.util.BambooServer;

/**
 * Plan configuration for Bamboo.
 * Learn more on: <a href="https://confluence.atlassian.com/display/BAMBOO/Bamboo+Specs">https://confluence.atlassian.com/display/BAMBOO/Bamboo+Specs</a>
 */
@BambooSpec
public class PlanSpec {
	
	private final static String PROJECT_NAME = "DEMO";
	private final static String PROJECT_KEY = "DEMO";
	private final static String DEPLOYMENT_KEY = "DEMO";
	private final static String BUILD_PLAN_NAME = "DEMO";
	private final static String BUILD_PLAN_KEY = "DEMO";
	
	public final Project createBuildProject() {
        return new Project()
                .name(PROJECT_NAME)
                .key(PROJECT_KEY);
	}
	
	public final Plan createBuildPlan()
	{
		Plan plan = new Plan(createBuildProject(), BUILD_PLAN_NAME, BUILD_PLAN_KEY);
		plan.description("Demo Build plan");
		plan.enabled(true);
		plan.planRepositories(new GitRepository()
                .name("verified repo")
                .url("ssh://git@http://ceala03374.emea.zurich.corp:7990:DEMO/deployment_scripts.git")
                .branch("verified"));
		
		return plan;
	}
	
	PlanPermissions createBuildPlanPermission(PlanIdentifier planIdentifier) {
        Permissions permission = new Permissions()
                .userPermissions("PCASTANOIGLE", PermissionType.ADMIN, PermissionType.CLONE, PermissionType.EDIT)
                .anonymousUserPermissionView();
        return new PlanPermissions(planIdentifier.getProjectKey(), planIdentifier.getPlanKey()).permissions(permission);
    }
	
	public final Deployment createDeploymentProject()
	{
		Deployment deployment = new Deployment(new PlanIdentifier(PROJECT_NAME, PROJECT_KEY), DEPLOYMENT_KEY);
		
		deployment.description("DEMO deployment plan");
		deployment.releaseNaming(new ReleaseNaming("release-1").autoIncrement(true));
		
		deployment.environments(
			new Environment("UAT")
                .description("UAT environment")
                .tasks(new CleanWorkingDirectoryTask(),
                    new ScriptTask()
                        .description("download deployment artifacts")
                        .inlineBody("mkdir \"deployment_scripts\"")),
						new ScriptTask()
                        .description("download deployment artifacts")
                        .inlineBody("echo \"download artifacts\"")
                        .workingSubdirectory("deployment_scripts")),),
            new Environment("PROD")
                .description("Production deployment")
                .tasks(new CleanWorkingDirectoryTask(),
                    new ScriptTask()
                        .description("download deployment artifacts")
                        .inlineBody("echo \"download artifacts\"")
                        .workingSubdirectory("deployment_scripts")));
		
		return deployment;
	}
	
	public DeploymentPermissions deploymentPermission() {
        final DeploymentPermissions deploymentPermission = new DeploymentPermissions(PROJECT_NAME)
            .permissions(new Permissions()
                    .userPermissions("PCASTANOIGLE", PermissionType.EDIT, PermissionType.VIEW)
                    .loggedInUserPermissions(PermissionType.VIEW)
                    .anonymousUserPermissionView());
        return deploymentPermission;
    }
    
    public EnvironmentPermissions environmentPermissionUAT() {
        final EnvironmentPermissions environmentPermission = new EnvironmentPermissions(PROJECT_NAME)
            .environmentName("UAT")
            .permissions(new Permissions()
                    .userPermissions("PCASTANOIGLE", PermissionType.EDIT, PermissionType.VIEW, PermissionType.BUILD)
                    .loggedInUserPermissions(PermissionType.VIEW)
                    .anonymousUserPermissionView());
        return environmentPermission;
    }
    
    public EnvironmentPermissions environmentPermissionPROD() {
        final EnvironmentPermissions environmentPermission = new EnvironmentPermissions(PROJECT_NAME)
            .environmentName("PROD")
            .permissions(new Permissions()
                    .userPermissions("PCASTANOIGLE", PermissionType.EDIT, PermissionType.VIEW, PermissionType.BUILD)
                    .loggedInUserPermissions(PermissionType.VIEW)
                    .anonymousUserPermissionView());
        return environmentPermission;
    }
	

    /**
     * Run main to publish plan on Bamboo
     */
    public static void main(final String[] args) throws Exception {
        //By default credentials are read from the '.credentials' file.
        BambooServer bambooServer = new BambooServer("http://localhost:8085");
        final PlanSpec planSpec = new PlanSpec();
        
      /*  // BUILD PLAN
        final Plan buildPlan = planSpec.createBuildPlan();
        bambooServer.publish(buildPlan);

        PlanPermissions planPermission = planSpec.createBuildPlanPermission(buildPlan.getIdentifier());
        bambooServer.publish(planPermission);
        */
        // DEPLOYMENT PLAN
        
        final Deployment deployment = planSpec.createDeploymentProject();
        bambooServer.publish(deployment);
        
        final DeploymentPermissions deploymentPermission = planSpec.deploymentPermission();
        bambooServer.publish(deploymentPermission);
        
        final EnvironmentPermissions environmentPermissionUAT = planSpec.environmentPermissionUAT();
        bambooServer.publish(environmentPermissionUAT);
        
        final EnvironmentPermissions environmentPermissionPROD = planSpec.environmentPermissionPROD();
        bambooServer.publish(environmentPermissionPROD);

    }

}
