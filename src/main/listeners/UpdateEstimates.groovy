import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.search.SearchProvider
import com.atlassian.jira.issue.search.SearchQuery
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.web.bean.PagerFilter

String jqlQuery = "project = TD"

// Set up Log
log.setLevel(org.apache.log4j.Level.DEBUG)

def issueManager = ComponentAccessor.issueManager;
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
def jqlQueryParser = ComponentAccessor.getComponent(JqlQueryParser);
def searchProvider = ComponentAccessor.getComponent(SearchProvider);
def query = jqlQueryParser.parseQuery(jqlQuery);
def searchQuery = SearchQuery.create(query, user);

def estimateTShirt = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Estimated Size")
def estimateInDays = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName("Estimate")

def results = searchProvider.search(searchQuery, PagerFilter.getUnlimitedFilter());
log.info "Issues cnt: ${results.getTotal()}";
results.getResults().collect { res ->
    def doc = res.getDocument();
    def key = doc.get("key");
    def issue = issueManager.getIssueObject(key);
    def issueKey = issue.key
    log.debug("Issue $issueKey")
    def effortText = issue.getCustomFieldValue(estimateTShirt).toString()
    log.debug("T-Shirt estimate: $effortText")
    double number = 0
    if (effortText == "XS") { number = 1 }
    else if (effortText == "S") { number = 5 }
    else if (effortText == "M") { number = 15 }
    else if (effortText == "L") { number = 40 }
    else if (effortText == "XL") { number = 160 }
    else if (effortText == "XXL") { number = 800 }
    else if (effortText == "XXXL") { number = 2400 }
    else { number = 0 }
    log.debug("Estimated days: $number")
    def changeHolder = new DefaultIssueChangeHolder()
    estimateInDays.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(estimateInDays), number),changeHolder)
}
