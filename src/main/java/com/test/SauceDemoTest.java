package com.test;

import com.microsoft.playwright.*;
import java.nio.file.Paths;
import java.io.FileWriter;

public class SauceDemoTest {

    static int clickCount = 0;
    static int totalTests = 0;
    static int passedTests = 0;

    static StringBuilder log = new StringBuilder();

    public static void logStep(String step) {
        System.out.println(step);
        log.append(step).append("\n");
    }

    public static void navigateLog(String step) {
        logStep("➡ Navigation: " + step);
    }

    public static void logTest(String name, boolean status) {
        totalTests++;
        if (status) {
            passedTests++;
            logStep(name + ": PASS");
        } else {
            logStep(name + ": FAIL");
        }
    }

    // ✅ FIXED CLICK (NO HOVER ISSUE)
    public static void click(Page page, Locator element, String name) {
        element.scrollIntoViewIfNeeded();
        page.waitForTimeout(300);

        element.click();
        clickCount++;

        logStep("Clicked: " + name);
        page.waitForTimeout(700);
    }

    public static void type(Page page, Locator element, String text, String name) {
        element.type(text, new Locator.TypeOptions().setDelay(50));
        logStep("Entered " + name + ": " + text);
        page.waitForTimeout(700);
    }

    public static void main(String[] args) throws Exception {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300)
            );

            BrowserContext context = browser.newContext();

            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));

            Page page = context.newPage();

            logStep("Browser launched");

            // 🌐 Open site
            page.navigate("https://www.saucedemo.com/");
            navigateLog("Opened Login Page");

            page.waitForTimeout(1500);

            // 🔐 Login
            type(page, page.locator("#user-name"), "standard_user", "Username");
            type(page, page.locator("#password"), "secret_sauce", "Password");

            click(page, page.locator("#login-button"), "Login");

            boolean loginStatus = page.locator(".inventory_list").isVisible();
            logTest("Test Case 1 - Login", loginStatus);

            navigateLog("Products Page");

            page.waitForTimeout(1500);

            // 🛒 STEP 1: OPEN FIRST PRODUCT
            click(page, page.locator(".inventory_item_name").first(), "Open First Product");

            navigateLog("Opened Product 1");
            page.waitForTimeout(3000); // 👁️ VIEW

            // 🔙 BACK
            click(page, page.locator("#back-to-products"), "Back to Products");

            navigateLog("Returned to Products");

            page.waitForTimeout(1500);

            // 📜 SCROLL
            page.mouse().wheel(0, 800);
            logStep("Scrolled down");

            page.waitForTimeout(1500);

            // 🛒 STEP 2: OPEN SECOND PRODUCT
            click(page, page.locator(".inventory_item_name").nth(2), "Open Second Product");

            navigateLog("Opened Product 2");
            page.waitForTimeout(3000); // 👁️ VIEW

            // 🔙 BACK AGAIN
            click(page, page.locator("#back-to-products"), "Back to Products");

            navigateLog("Returned to Products Again");

            page.waitForTimeout(1500);

            // 🛒 ADD TO CART (AFTER VIEWING)
            page.waitForSelector(".inventory_item");

            Locator addBtn = page.locator(".inventory_item").nth(2).locator("button");

            click(page, addBtn, "Add to Cart");

            boolean cartAdded = page.locator(".shopping_cart_badge").isVisible();
            logTest("Test Case 2 - Add to Cart", cartAdded);

            // 🛒 CART
            click(page, page.locator(".shopping_cart_link"), "Open Cart");

            navigateLog("Cart Page");

            page.waitForTimeout(1500);

            // CHECKOUT
            click(page, page.locator("#checkout"), "Checkout");

            navigateLog("Checkout Page");

            boolean checkoutPage = page.locator("#first-name").isVisible();
            logTest("Test Case 3 - Checkout Page", checkoutPage);

            page.waitForTimeout(1500);

            // 📝 DETAILS
            type(page, page.locator("#first-name"), "Srilaya", "First Name");
            type(page, page.locator("#last-name"), "M", "Last Name");
            type(page, page.locator("#postal-code"), "632014", "Postal Code");

            click(page, page.locator("#continue"), "Continue");

            page.waitForTimeout(1500);

            // FINISH
            click(page, page.locator("#finish"), "Finish");

            navigateLog("Order Confirmation Page");

            boolean orderSuccess = page.locator(".complete-header").isVisible();
            logTest("Test Case 4 - Order Completion", orderSuccess);

            page.waitForTimeout(2000);

            // 📸 SCREENSHOT
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("final.png")));
            logStep("Screenshot captured");

            // TRACE
            context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace.zip")));

            // 📄 REPORT
            FileWriter writer = new FileWriter("Report.txt");

            writer.write("===== AUTOMATION EXECUTION REPORT =====\n\n");

            writer.write("----- Navigation Flow -----\n");
            writer.write("Login → Products → Product1 → Products → Product2 → Products → Cart → Checkout → Confirmation\n\n");

            writer.write("----- Execution Log -----\n");
            writer.write(log.toString());

            writer.write("\n===== TEST SUMMARY =====\n");
            writer.write("Total Tests: " + totalTests + "\n");
            writer.write("Passed: " + passedTests + "\n");
            writer.write("Failed: " + (totalTests - passedTests) + "\n");

            writer.write("\nTotal Clicks: " + clickCount);

            writer.close();

            logStep("Report generated");

            System.out.println("\n===== FINAL RESULT =====");
            System.out.println("Total Tests: " + totalTests);
            System.out.println("Passed: " + passedTests);
            System.out.println("Failed: " + (totalTests - passedTests));

            System.out.println("\nExecution Completed Successfully!");

            browser.close();
        }
    }
}