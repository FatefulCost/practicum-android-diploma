package ru.practicum.android.diploma.ui.vacancydetail

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.practicum.android.diploma.data.dto.ContactsDto

object ContactHelper {

    data class ContactViews(
        val emailView: TextView,
        val phoneView: TextView,
        val emailLayout: View,
        val phoneLayout: View,
        val phoneCommentView: TextView
    )

    fun setupContacts(
        fragment: Fragment,
        contacts: ContactsDto?,
        views: ContactViews
    ) {
        if (contacts == null) {
            hideAll(views.emailLayout, views.phoneLayout)
            return
        }

        setupEmail(fragment, contacts.email, views.emailView, views.emailLayout)
        setupPhone(fragment, contacts, views.phoneView, views.phoneLayout, views.phoneCommentView)
    }

    private fun setupEmail(
        fragment: Fragment,
        email: String?,
        tvEmail: TextView,
        layoutEmail: View
    ) {
        email?.let { safeEmail ->
            layoutEmail.visibility = View.VISIBLE
            tvEmail.text = safeEmail
            tvEmail.setOnClickListener { openEmail(fragment, safeEmail) }
        } ?: run {
            layoutEmail.visibility = View.GONE
        }
    }

    private fun setupPhone(
        fragment: Fragment,
        contacts: ContactsDto,
        tvPhone: TextView,
        layoutPhone: View,
        tvPhoneComment: TextView
    ) {
        contacts.phone?.firstOrNull()?.let { firstPhone ->
            layoutPhone.visibility = View.VISIBLE
            tvPhone.text = firstPhone
            tvPhone.setOnClickListener { openPhone(fragment, firstPhone) }

            contacts.name?.let { contactName ->
                tvPhoneComment.text = contactName
                tvPhoneComment.visibility = View.VISIBLE
            } ?: run {
                tvPhoneComment.visibility = View.GONE
            }
        } ?: run {
            layoutPhone.visibility = View.GONE
        }
    }

    private fun hideAll(vararg views: View) {
        views.forEach { it.visibility = View.GONE }
    }

    fun openEmail(fragment: Fragment, email: String) {
        if (email.isBlank()) return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        if (intent.resolveActivity(fragment.requireContext().packageManager) != null) {
            fragment.startActivity(intent)
        } else {
            Toast.makeText(fragment.requireContext(), "Нет приложения для отправки писем", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPhone(fragment: Fragment, phone: String) {
        val cleanedPhone = phone.replace(Regex("[^\\d+]"), "")
        if (cleanedPhone.isBlank()) return

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanedPhone")
        }
        if (intent.resolveActivity(fragment.requireContext().packageManager) != null) {
            fragment.startActivity(intent)
        } else {
            Toast.makeText(fragment.requireContext(), "Нет приложения для звонков", Toast.LENGTH_SHORT).show()
        }
    }
}
