/*
 * Jira script for copying components from one master project to other projects
 * Added as a listener to Jira on ProjectComponentCreated
 * Adapted from script on mraddon.blog
 */

def destinationProjects = ["DEV15"]
def sourceProject = "TD"

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.bc.project.component.ProjectComponent

def projectManager = ComponentAccessor.getProjectManager()
def projectSource = projectManager.getProjectObjByKey(sourceProject).getId()
def projectComponentManager = ComponentAccessor.getProjectComponentManager()
Collection<ProjectComponent> componentList =
        projectComponentManager.findAllForProject(projectSource) as Collection<ProjectComponent>

def i = 0
while (i < destinationProjects.size()) {
    def projectDestinationList = projectManager.getProjectObjByKey(destinationProjects[i])
    def project = projectDestinationList

    if (componentList != null) {
        for (component in componentList) {
            //log.debug("Component " + component.getName() )
            def componentTemp = projectComponentManager.findByComponentName(project.getId(), component.getName())
            if (componentTemp == null) {
                //log.debug("Adding component " + component.getName() + " to " + project.getName())
                def componentTempResult =
                        projectComponentManager.create(
                                component.getName(),
                                component.getDescription(),
                                component.getLead(),
                                4,
                                project.getId())
                // 1= COMPONENT_LEAD, 2= PROJECT_DEFAULT, 3 =PROJECT_LEAD , 4= UNASSIGNED
            }
        }
    }
    i++
}
