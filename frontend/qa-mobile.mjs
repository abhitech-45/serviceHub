export default async function run(page) {
  await page.setViewportSize({ width: 390, height: 844 });
  return {
    viewport: await page.evaluate(() => ({ width: window.innerWidth, height: window.innerHeight })),
    authTitle: await page.getByRole('heading', { name: 'Sign in to your portal' }).innerText(),
    horizontalOverflow: await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth),
  };
}
