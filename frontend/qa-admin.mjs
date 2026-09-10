export default async function run(page) {
  await page.getByLabel('Email address').fill('admin@servicehub.local');
  await page.getByLabel('Password').fill('Admin@12345');
  await page.getByRole('button', { name: 'Enter workspace' }).click();
  await page.getByRole('heading', { name: 'See the service system clearly.' }).waitFor();
  return {
    path: await page.evaluate(() => window.location.pathname),
    heading: await page.getByRole('heading', { name: 'See the service system clearly.' }).innerText(),
    totalUsers: await page.getByText('Total users').isVisible(),
    totalRequests: await page.getByText('Total requests').isVisible(),
    allRequests: await page.getByRole('heading', { name: 'All service requests' }).isVisible(),
    actionSelects: await page.locator('.admin-request-actions select').count(),
    criticalHighlight: await page.locator('.admin-request.critical').count(),
  };
}
