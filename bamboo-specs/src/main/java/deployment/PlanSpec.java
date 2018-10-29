package deployment;

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.credentials.SharedCredentialsIdentifier;
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
	
	
	// *** PLAN DETAILS *** //
	// Name of the project
	private final static String PROJECT_NAME = "PROJECTNAME";
	// Project key
	private final static String PROJECT_KEY = "PROJECTKEY";
	// Deployment project key
	private final static String DEPLOYMENT_KEY = "DEPLOYKEY";
	// Deployment project description
	private final static String DEPLOYMENT_PLAN_DESC = "Description of the deployment plan";
	// Release name format
	private final static String RELEASE_NAME_FORMAT = "Release-1";
	// Name of the build plan
	private final static String BUILD_PLAN_NAME = "BUILDPLANNAME";
	// Build plan key
	private final static String BUILD_PLAN_KEY = "BUILDPLANKEY";
	// Build plan description
	private final static String BUILD_PLAN_DESC = "Description of the build plan";
	// Bamboo URL
	private final static String BAMBOO_URL = "http://localhost:8085";
	
	
	// UAT environment name
	private final static String UAT_ENV = "UAT";
	// PROD environment name
	private final static String PROD_ENV = "PROD";
	
	
	// *** CERTIFIED REPOSITORY DETAILS *** //	
	// Repo name
	private final static String REPO_NAME = "Verified repo";
	// Repo URL
	private final static String REPO_URL = "ssh://git@http://ceala03374.emea.zurich.corp:7990:DEMO/deployment_scripts.git";
	// Repo branch
	private final static String REPO_BRANCH = "verified";
	
	// *** GROUPS AND USERS FOR PRIVILEGES *** //
	// DXC platform administrators
	private final static String DXC_ADMINISTRATORS = "CEGSEC_DEVOPS_GE_DEVOPS_ADMINISTRATORS";
	// DXC reviewers and teams
	private final static String DXC_CONTINUOUS_DEPLOYMENT_TEAM = "CEGSEC_DEVOPS_GE_CD_DEVELOPERS";
	// Project team
	private final static String PROJECT_TEAM = "CEGSEC_DEVOPS_GE_CD_USERS";
	
	
	
	
	
	
	/*** STANDARD METHODS - NOT MODIFY ***/	
	public final Project createBuildProject() {
        return new Project()
                .name(PROJECT_NAME)
                .key(PROJECT_KEY);
	}
	
	public final Plan createBuildPlan()
	{
		Plan plan = new Plan(createBuildProject(), BUILD_PLAN_NAME, BUILD_PLAN_KEY);
		plan.description(BUILD_PLAN_DESC);
		plan.enabled(true);
		plan.planRepositories(new GitRepository()
                .name(REPO_NAME)
                .url(REPO_URL)
                .branch(REPO_BRANCH)
                .authentication(new SharedCredentialsIdentifier("VERIFIED_REPO")));
		
		return plan;
	}
	
	PlanPermissions createBuildPlanPermission(PlanIdentifier planIdentifier) {
        Permissions permission = new Permissions()
        		.groupPermissions(DXC_ADMINISTRATORS, PermissionType.VIEW, PermissionType.EDIT, PermissionType.BUILD, PermissionType.CLONE, PermissionType.ADMIN) 
                .groupPermissions(DXC_CONTINUOUS_DEPLOYMENT_TEAM, PermissionType.VIEW, PermissionType.EDIT, PermissionType.BUILD)
                .groupPermissions(PROJECT_TEAM, PermissionType.VIEW, PermissionType.BUILD);
        return new PlanPermissions(planIdentifier.getProjectKey(), planIdentifier.getPlanKey()).permissions(permission);
    }
	
	public DeploymentPermissions deploymentPermission() {
        final DeploymentPermissions deploymentPermission = new DeploymentPermissions(DEPLOYMENT_KEY)
            .permissions(new Permissions()
			        .groupPermissions(DXC_ADMINISTRATORS,PermissionType.VIEW, PermissionType.EDIT)
			        .groupPermissions(DXC_CONTINUOUS_DEPLOYMENT_TEAM, PermissionType.VIEW, PermissionType.EDIT)
			        .groupPermissions(PROJECT_TEAM, PermissionType.VIEW));
        
        return deploymentPermission;
    }
    
    public EnvironmentPermissions environmentPermissionUAT() {
        final EnvironmentPermissions environmentPermission = new EnvironmentPermissions(DEPLOYMENT_KEY)
            .environmentName(UAT_ENV)
            .permissions(new Permissions()
                    .groupPermissions(DXC_ADMINISTRATORS, PermissionType.VIEW, PermissionType.EDIT, PermissionType.BUILD)
                    .groupPermissions(DXC_CONTINUOUS_DEPLOYMENT_TEAM, PermissionType.VIEW, PermissionType.EDIT, PermissionType.BUILD)
                    .groupPermissions(PROJECT_TEAM, PermissionType.VIEW, PermissionType.BUILD));
        return environmentPermission;
    }
    
    public EnvironmentPermissions environmentPermissionPROD() {
        final EnvironmentPermissions environmentPermission = new EnvironmentPermissions(DEPLOYMENT_KEY)
            .environmentName(PROD_ENV)
            .permissions(new Permissions()
            		.groupPermissions(DXC_ADMINISTRATORS, PermissionType.VIEW, PermissionType.EDIT, PermissionType.BUILD)
                    .groupPermissions(DXC_CONTINUOUS_DEPLOYMENT_TEAM, PermissionType.VIEW, PermissionType.EDIT, PermissionType.BUILD)
                    .groupPermissions(PROJECT_TEAM, PermissionType.VIEW));
        return environmentPermission;
    }
	

    /**
     * Run main to publish plan on Bamboo
     */
    public static void main(final String[] args) throws Exception {
        //By default credentials are read from the '.credentials' file.
        BambooServer bambooServer = new BambooServer(BAMBOO_URL);
        final PlanSpec planSpec = new PlanSpec();
        
        // BUILD PLAN
        final Plan buildPlan = planSpec.createBuildPlan();
        bambooServer.publish(buildPlan);
        
        

        PlanPermissions planPermission = planSpec.createBuildPlanPermission(buildPlan.getIdentifier());
        bambooServer.publish(planPermission);
                
        // DEPLOYMENT PLAN
        final PlanSpec deployPlanSpec = new PlanSpec();
        
        final Deployment deployment = deployPlanSpec.createDeploymentProject();
        bambooServer.publish(deployment);
        
        final DeploymentPermissions deploymentPermission = deployPlanSpec.deploymentPermission();
        bambooServer.publish(deploymentPermission);
        
        final EnvironmentPermissions environmentPermissionUAT = deployPlanSpec.environmentPermissionUAT();
        bambooServer.publish(environmentPermissionUAT);
        
        final EnvironmentPermissions environmentPermissionPROD = deployPlanSpec.environmentPermissionPROD();
        bambooServer.publish(environmentPermissionPROD);

    }
    
    //*** END OF NOT MODIFY AREA
    
    //*** AREA TO BE MODIFIED BY PROJECT TEAM (TASKS PER EACH ENVIRONMENT ***//
    public final Deployment createDeploymentProject()
	{
		Deployment deployment = new Deployment(new PlanIdentifier(PROJECT_KEY, BUILD_PLAN_KEY), DEPLOYMENT_KEY);
		
		deployment.description(DEPLOYMENT_PLAN_DESC);
		deployment.releaseNaming(new ReleaseNaming(RELEASE_NAME_FORMAT).autoIncrement(true));
		
		//*** MODIFY THIS ***//
		/* TODO: Modify tasks for UAT and PROD environments */
		deployment.environments(
			new Environment(UAT_ENV)
                .description("UAT environment")
                .tasks(new CleanWorkingDirectoryTask(),
                    new ScriptTask()
                        .description("download deployment artifacts")
                        .inlineBody("mkdir \"deployment_scripts\""),
						new ScriptTask()
                        .description("download deployment artifacts")
                        .inlineBody("echo \"download artifacts\"")
                        .workingSubdirectory("deployment_scripts")),
            new Environment(PROD_ENV)
                .description("Production deployment")
                .tasks(new CleanWorkingDirectoryTask(),
                    new ScriptTask()
                        .description("download deployment artifacts")
                        .inlineBody("echo \"download artifacts\"")
                        .workingSubdirectory("deployment_scripts")));
		
		//*** END OF MODIFY THIS
		return deployment;
	}
    

}