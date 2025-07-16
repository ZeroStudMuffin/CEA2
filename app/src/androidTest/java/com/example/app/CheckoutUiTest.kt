package com.example.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CheckoutUiTest {
    @Test
    fun addItem_enablesCheckoutButton() {
        val intent = android.content.Intent(InstrumentationRegistry.getInstrumentation().targetContext, CheckoutActivity::class.java)
        intent.putExtra("pin", "1234")
        ActivityScenario.launch<CheckoutActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val methodShow = CheckoutActivity::class.java.getDeclaredMethod("showResult", List::class.java)
                methodShow.isAccessible = true
                methodShow.invoke(activity, listOf("Roll#:1", "Cust:ACME"))
                val methodAdd = CheckoutActivity::class.java.getDeclaredMethod("onAddItem")
                methodAdd.isAccessible = true
                methodAdd.invoke(activity)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            onView(withId(R.id.checkoutButton)).check(matches(isEnabled()))
        }
    }
}
