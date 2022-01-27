// issue is defined automatically when used as a post-function
import com.atlassian.jira.component.ComponentAccessor

def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

if(!issue.assignee){
    issue.setAssignee((currentUser))
}
