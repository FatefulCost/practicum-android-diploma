package ru.practicum.android.diploma.ui.vacancydetail

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import ru.practicum.android.diploma.data.dto.ContactsDto

object ContactHelper {

    fun setupContacts(
        fragment: Fragment,
        contacts: ContactsDto?,
        tvEmail: TextView,
        tvPhone: TextView,
        layoutEmail: View,
        layoutPhone: View,
        tvPhoneComment: TextView
    ) {
        if (contacts == null) {
            hideAll(layoutEmail, layoutPhone)
            return
        }

        val hasEmail = !contacts.email.isNullOrEmpty()
        val hasPhone = contacts.phone != null && contacts.phone.isNotEmpty()

        if (!hasEmail && !hasPhone) {
            hideAll(layoutEmail, layoutPhone)
            return
        }

        setupEmail(fragment, contacts, tvEmail, layoutEmail, hasEmail)
        setupPhone(fragment, contacts, tvPhone, layoutPhone, tvPhoneComment, hasPhone)
    }

    private fun setupEmail(
        fragment: Fragment,
        contacts: ContactsDto,
        tvEmail: TextView,
        layoutEmail: View,
        hasEmail: Boolean
    ) {
        if (hasEmail) {
            layoutEmail.visibility = View.VISIBLE
            tvEmail.text = contacts.email
            tvEmail.setOnClickListener {
                openEmail(fragment, contacts.email!!)
            }
        } else {
            layoutEmail.visibility = View.GONE
        }
    }

    private fun setupPhone(
        fragment: Fragment,
        contacts: ContactsDto,
        tvPhone: TextView,
        layoutPhone: View,
        tvPhoneComment: TextView,
        hasPhone: Boolean
    ) {
        if (hasPhone) {
            layoutPhone.visibility = View.VISIBLE
            val firstPhone = contacts.phone!!.first()
            tvPhone.text = firstPhone
            tvPhone.setOnClickListener {
                openPhone(fragment, firstPhone)
            }

            if (!contacts.name.isNullOrEmpty()) {
                tvPhoneComment.text = contacts.name
                tvPhoneComment.visibility = View.VISIBLE
            } else {
                tvPhoneComment.visibility = View.GONE
            }
        } else {
            layoutPhone.visibility = View.GONE
        }
    }

    private fun hideAll(vararg views: View) {
        views.forEach { it.visibility = View.GONE }
    }

    fun openEmail(fragment: Fragment, email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        fragment.startActivity(Intent.createChooser(intent, "Отправить письмо"))
    }

    fun openPhone(fragment: Fragment, phone: String) {
        val cleanedPhone = phone.replace(Regex("[^\\d+]"), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanedPhone")
        }
        fragment.startActivity(Intent.createChooser(intent, "Выберите приложение для звонка"))
    }
}
