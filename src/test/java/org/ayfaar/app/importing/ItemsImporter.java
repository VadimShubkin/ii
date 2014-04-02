package org.ayfaar.app.importing;

import org.apache.commons.io.FileUtils;
import org.ayfaar.app.dao.ItemDao;
import org.ayfaar.app.model.Item;
import org.ayfaar.app.utils.UriGenerator;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;

public class ItemsImporter {
    private static Item currentItem;
    private static Item prevItem;
    private static ApplicationContext ctx;
//    private static String skipUntilNumber = "1.0780";
    private static boolean saveAllowed = true;

    public static void main(String[] args) throws Docx4JException, IOException {
        currentItem = null;

        ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        for(String line: FileUtils.readLines(new File("D:\\PROJECTS\\ayfaar\\ii-app\\src\\main\\text\\Том 15.txt"))) {

            Matcher matcher = compile("(\\d+\\.\\d+)\\.\\s(.+)").matcher(line);
            if (matcher.find()) {
                if (currentItem != null && saveAllowed) {
                    saveItem();
                }
                currentItem = new Item(matcher.group(1), matcher.group(2));
//                saveAllowed = saveAllowed || currentItem.getNumber().equals(skipUntilNumber);
            } else if (currentItem != null) {
                currentItem.setContent(currentItem.getContent() + line);
            }
        }
        saveItem();
    }

    private static void saveItem() {
        ItemDao itemDao = ctx.getBean(ItemDao.class);
        currentItem.setContent(currentItem.getContent().trim());
        System.out.print(currentItem.getNumber() + ": ");
        System.out.println(currentItem.getContent());

        Item storedItem = itemDao.getByNumber(currentItem.getNumber());
        if (storedItem != null) {
            currentItem.setUri(UriGenerator.generate(currentItem));
        }
        itemDao.save(currentItem);

        if (prevItem != null) {
            prevItem.setNext(currentItem.getUri());
            itemDao.save(prevItem);
        }

        prevItem = currentItem;
    }
}
