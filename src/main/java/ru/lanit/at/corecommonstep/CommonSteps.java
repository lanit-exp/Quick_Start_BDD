package ru.lanit.at.corecommonstep;

import io.cucumber.java.ru.И;
import ru.lanit.at.corecommonstep.fragment.FragmentReplacer;


public class CommonSteps {

    @И(FragmentReplacer.REGEX_FRAGMENT)
    public void userInsertsFragment(String fragmentName) {
        throw new IllegalStateException("фрагмент не подставился");
    }
}
