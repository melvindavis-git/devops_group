# DevOps Group

## Environments

| Environment | URL |
|-------------|-----|
| Staging | https://devopsgroup-staging.up.railway.app/ |
| Production | https://devopsgroup-production.up.railway.app/ |

## Branch Strategy

In this project we have worked with the **GitHub Flow** branch strategy. 
That means that when we work on a new feature or bug fix, it is done in shortlived branches. 
When these branches are ready, they are pushed to GitHub and a pull request is created, which is then reviewed by a group member. 
When the pull request is approved, it is merged into our `master` branch and then automatically deployed to our staging environment. 
When we deem that the latest staging version is ready for production, we manually trigger the deployment via a git tag.

### Why GitHub Flow?

We chose to work with GitHub Flow because it is easy to work with and suits small groups like ours. 
Since the branches are so shortlived, merge conflicts tend to be quite minor and not overly disruptive. 
Code reviews are a nice addition to this type of workflow, but they don't add too much value for us since our changes are usually on the smaller side.

### Workflow

1. Create a new branch from `master` for your feature or bug fix
2. Push the branch to GitHub and open a pull request
3. Get the pull request reviewed and approved by a group member
4. Merge into `master`, which automatically deploys to staging
5. Create a git tag to deploy to production

## Merge Conflicts

For our merge conflict two of us both worked in the same file and changed the same variable in different branches.
First one of the branches was merged into our main, when we then tried to merge the second branch we got a warning
that there was a conflict between the two. We used the GitHub web editor to resolve the conflict by accepting the
incoming changes.

## Rollback

> **TODO:** Add information about how we handle rollbacks.