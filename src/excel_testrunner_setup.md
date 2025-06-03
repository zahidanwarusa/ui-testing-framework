# TestRunner.xlsx Setup

Create a file called `TestRunner.xlsx` in `src/main/resources/excel/` with the following structure:

## Sheet Name: TestCases

| TestID | TestName | Description | Execute | JiraTicket |
|--------|----------|-------------|---------|------------|
| CBP001 | CBP Login Test | Test CBP login functionality | Y | CBP-1234 |
| CBP002 | Create 1-Day Lookout | Complete workflow to create 1-day lookout | Y | CBP-1235 |
| CBP003 | TECS ID Validation | Validate TECS ID generation and capture | Y | CBP-1236 |

**Column Descriptions:**
- **TestID**: Unique identifier for each test
- **TestName**: Descriptive name for the test
- **Description**: Brief description of what the test does
- **Execute**: Y = Run this test, N = Skip this test
- **JiraTicket**: Associated JIRA ticket number for tracking

**JIRA Ticket Format Examples:**
- CBP-1234 (for CBP project tickets)
- PROJ-567 (for other project tickets)
- Leave blank if no JIRA ticket associated