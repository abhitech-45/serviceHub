export default async function run(page) {
  const email = `browser-${Date.now()}@example.com`;
  await page.getByRole('button', { name: 'New here? Create an account' }).click();
  await page.getByLabel('Display name').fill('Browser Customer');
  await page.getByLabel('Email address').fill(email);
  await page.getByLabel('Password').fill('password123');
  await page.getByRole('button', { name: 'Create account' }).click();
  await page.getByRole('heading', { name: /Good to see you/ }).waitFor();
  await page.getByLabel('Subject').fill('Browser request');
  await page.getByLabel('What do you need help with?').fill('Testing the connected ServiceHub workflow.');
  await page.getByRole('button', { name: 'Submit request' }).click();
  await page.getByText('Browser request').waitFor();
  return {
    heading: await page.getByRole('heading', { name: /Good to see you/ }).innerText(),
    requestVisible: await page.getByText('Browser request').isVisible(),
    requestCount: await page.locator('.request-row').count(),
    selectedSubject: await page.getByLabel('Subject').inputValue(),
    selectedDescription: await page.getByLabel('What do you need help with?').inputValue(),
  };
}
