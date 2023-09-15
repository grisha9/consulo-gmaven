package consulo.gmaven.settings;

import consulo.application.util.SystemInfo;
import consulo.configurable.ConfigurationException;
import consulo.disposer.Disposable;
import consulo.externalSystem.ui.awt.ExternalSystemUiUtil;
import consulo.externalSystem.ui.awt.PaintAwarePanel;
import consulo.gmaven.Constants;
import consulo.gmaven.MavenLog;
import consulo.gmaven.util.MavenUtils;
import consulo.gmaven.wrapper.MvnDotProperties;
import consulo.ide.impl.idea.openapi.externalSystem.service.settings.AbstractExternalProjectSettingsControl;
import consulo.ide.impl.idea.openapi.roots.ui.configuration.projectRoot.DefaultSdksModel;
import consulo.ide.impl.idea.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import consulo.ide.impl.idea.openapi.roots.ui.util.CompositeAppearance;
import consulo.module.ui.awt.SdkComboBox;
import consulo.project.Project;
import consulo.ui.ex.JBColor;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.awt.*;
import consulo.util.lang.StringUtil;
import consulo.util.nodep.SystemInfoRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static consulo.externalSystem.ui.awt.ExternalSystemUiUtil.getLabelConstraints;
import static consulo.fileChooser.FileChooserDescriptorFactory.createSingleFolderDescriptor;
import static consulo.gmaven.MavenBundle.message;
import static consulo.gmaven.settings.DistributionType.CUSTOM;
import static consulo.ui.ex.awt.TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT;
import static java.util.Objects.requireNonNullElse;

public class ProjectSettingsControl extends AbstractExternalProjectSettingsControl<MavenProjectSettings> {
    @Nullable
    private JBCheckBox nonRecursiveCheckBox;
    @Nullable
    private JBCheckBox updateSnapshotsCheckBox;
    @Nullable
    private JBCheckBox useWholeProjectContextCheckBox;
    @Nullable
    private JBCheckBox resolveModulePerSourceSetCheckBox;
    @Nullable
    private JBCheckBox showPluginNodesCheckBox;
    @Nullable
    private JTextField threadCountField;
    @Nullable
    private JTextField vmOptionsField;
    @Nullable
    private JTextField argumentsField;
    @Nullable
    private JTextField argumentsImportField;
    @Nullable
    private ComboBox<OutputLevelComboBoxItem> outPutLevelCombobox;
    @Nullable
    private ComboBox<DistributionSettingsComboBoxItem> mavenHomeCombobox;
    @Nullable
    private TextFieldWithBrowseButton mavenCustomPathField;
    @Nullable
    private JBLabel wrapperHintLabel;

    private SdkComboBox jdkComboBox;
    private JPanel jdkComboBoxWrapper;

    @Nonnull
    private final Project project;

    public ProjectSettingsControl(@Nonnull Project project, @Nonnull MavenProjectSettings initialSettings) {
        super(initialSettings);
        this.project = project;
    }

    @Override
    protected void fillExtraControls(@Nonnull Disposable disposable, @Nonnull PaintAwarePanel content, int indentLevel) {
        nonRecursiveCheckBox = new JBCheckBox(message("gmaven.settings.project.recursive"));
        content.add(nonRecursiveCheckBox, ExternalSystemUiUtil.getFillLineConstraints(indentLevel));

        updateSnapshotsCheckBox = new JBCheckBox(message("gmaven.settings.project.update"));
        content.add(updateSnapshotsCheckBox, ExternalSystemUiUtil.getFillLineConstraints(indentLevel));

        useWholeProjectContextCheckBox = new JBCheckBox(message("gmaven.settings.project.task.context"));
        useWholeProjectContextCheckBox.setToolTipText(message("gmaven.settings.project.task.context.tooltip"));
        content.add(useWholeProjectContextCheckBox, ExternalSystemUiUtil.getFillLineConstraints(indentLevel));

        resolveModulePerSourceSetCheckBox = new JBCheckBox(message("gmaven.settings.project.module.per.source.set"));
        content.add(resolveModulePerSourceSetCheckBox, ExternalSystemUiUtil.getFillLineConstraints(indentLevel));

        showPluginNodesCheckBox = new JBCheckBox(message("gmaven.settings.project.plugins"));
        showPluginNodesCheckBox.setToolTipText(message("gmaven.settings.project.plugins.tooltip"));
        content.add(showPluginNodesCheckBox, ExternalSystemUiUtil.getFillLineConstraints(indentLevel));

        outPutLevelCombobox = setupOutputLevelComboBox();
        JBLabel outputLevelLabel = new JBLabel(message("gmaven.settings.project.output.level"));
        content.add(outputLevelLabel, getLabelConstraints(indentLevel));
        content.add(outPutLevelCombobox, getLabelConstraints(0));
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
        outputLevelLabel.setLabelFor(outPutLevelCombobox);


        JBLabel threadCountLabel = new JBLabel(message("gmaven.settings.project.thread.count"));
        threadCountField = new JTextField();
        content.add(threadCountLabel, getLabelConstraints(indentLevel));
        content.add(threadCountField, getLabelConstraints(0));
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
        threadCountLabel.setLabelFor(threadCountField);


        JBLabel vmOptionsLabel = new JBLabel(message("gmaven.settings.project.vm.options"));
        vmOptionsField = new JTextField();
        content.add(vmOptionsLabel, getLabelConstraints(indentLevel));
        content.add(vmOptionsField, getLabelConstraints(0));
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
        vmOptionsLabel.setLabelFor(vmOptionsField);

        JBLabel argumentsLabel = new JBLabel(message("gmaven.settings.project.arguments"));
        argumentsLabel.setToolTipText(message("gmaven.settings.project.arguments.tooltip"));
        argumentsField = new JTextField();
        argumentsField.setToolTipText(message("gmaven.settings.project.arguments.tooltip"));
        content.add(argumentsLabel, getLabelConstraints(indentLevel));
        content.add(argumentsField, getLabelConstraints(0));
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
        argumentsLabel.setLabelFor(argumentsField);

        JBLabel argumentsImportLabel = new JBLabel(message("gmaven.settings.project.arguments.import"));
        argumentsImportLabel.setToolTipText(message("gmaven.settings.project.arguments.import.tooltip"));
        argumentsImportField = new JTextField();
        argumentsImportField.setToolTipText(message("gmaven.settings.project.arguments.import.tooltip"));
        content.add(argumentsImportLabel, getLabelConstraints(indentLevel));
        content.add(argumentsImportField, getLabelConstraints(0));
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
        argumentsImportLabel.setLabelFor(argumentsImportField);


        JBLabel jdkLabel = new JBLabel(message("gmaven.settings.project.jvm"));
        jdkComboBoxWrapper = new JPanel(new BorderLayout());
        jdkLabel.setLabelFor(jdkComboBoxWrapper);
        content.add(jdkLabel, getLabelConstraints(indentLevel));
        content.add(jdkComboBoxWrapper, getLabelConstraints(0));
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));


        JBLabel mavenHomeLabel = new JBLabel(message("gmaven.settings.project.maven.home"));
        mavenHomeCombobox = setupMavenHomeComboBox();
        content.add(mavenHomeLabel, getLabelConstraints(indentLevel));
        content.add(mavenHomeCombobox, getLabelConstraints(0));
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
        mavenHomeLabel.setLabelFor(mavenHomeCombobox);

        JBLabel hintLabel = new JBLabel("", UIUtil.ComponentStyle.MINI);
        wrapperHintLabel = new JBLabel("", UIUtil.ComponentStyle.MINI);
        GridBag constraints = getLabelConstraints(indentLevel);
        constraints.insets.top = 0;
        content.add(hintLabel, constraints);
        constraints = getLabelConstraints(0);
        constraints.insets.top = 0;
        content.add(wrapperHintLabel, constraints);
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
        hintLabel.setLabelFor(wrapperHintLabel);

        JBLabel mavenCustomPathLabel = new JBLabel();
        mavenCustomPathField = new TextFieldWithBrowseButton();
        content.add(mavenCustomPathLabel, getLabelConstraints(indentLevel));
        content.add(mavenCustomPathField, getLabelConstraints(0));
        content.add(Box.createGlue(), ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
        mavenCustomPathLabel.setLabelFor(mavenCustomPathField);
    }

    @Override
    protected boolean isExtraSettingModified() {
        MavenProjectSettings projectSettings = getInitialSettings();
        if (nonRecursiveCheckBox != null
                && nonRecursiveCheckBox.isSelected() != projectSettings.isNonRecursive()) {
            return true;
        }
        if (updateSnapshotsCheckBox != null
                && updateSnapshotsCheckBox.isSelected() != projectSettings.isUpdateSnapshots()) {
            return true;
        }
        if (useWholeProjectContextCheckBox != null
                && useWholeProjectContextCheckBox.isSelected() != projectSettings.isUseWholeProjectContext()) {
            return true;
        }
        if (resolveModulePerSourceSetCheckBox != null
                && resolveModulePerSourceSetCheckBox.isSelected() != projectSettings.isResolveModulePerSourceSet()) {
            return true;
        }
        if (showPluginNodesCheckBox != null
                && showPluginNodesCheckBox.isSelected() != projectSettings.isShowPluginNodes()) {
            return true;
        }
        if (threadCountField != null && !Objects.equals(
                threadCountField.getText(), requireNonNullElse(projectSettings.getThreadCount(), ""))
        ) {
            return true;
        }
        if (vmOptionsField != null && !Objects.equals(
                vmOptionsField.getText(), requireNonNullElse(projectSettings.getVmOptions(), ""))
        ) {
            return true;
        }
        if (argumentsField != null && !Objects.equals(
                argumentsField.getText(), requireNonNullElse(projectSettings.getArguments(), ""))
        ) {
            return true;
        }
        if (argumentsImportField != null && !Objects.equals(
                argumentsImportField.getText(), requireNonNullElse(projectSettings.getArgumentsImport(), ""))
        ) {
            return true;
        }
        if (outPutLevelCombobox != null && outPutLevelCombobox.getSelectedItem() instanceof OutputLevelComboBoxItem
                && !Objects.equals(((OutputLevelComboBoxItem) outPutLevelCombobox.getSelectedItem()).value, projectSettings.getOutputLevel())) {
            return true;
        }
        if (jdkComboBox != null
                && !Objects.equals(jdkComboBox.getSelectedSdkName(), projectSettings.getJdkName())) {
            return true;
        }
        if (mavenHomeCombobox != null && mavenCustomPathField != null
                && mavenHomeCombobox.getSelectedItem() instanceof DistributionSettingsComboBoxItem) {
            var distributionSettings = ((DistributionSettingsComboBoxItem) mavenHomeCombobox.getSelectedItem()).value;
            DistributionSettings current = projectSettings.getDistributionSettings();
            if (distributionSettings.getType() == CUSTOM && current.getType() == CUSTOM
                    && !Objects.equals(Path.of(mavenCustomPathField.getText()), current.getPath())) {
                return true;
            } else return !Objects.equals(distributionSettings.getType(), current.getType());
        }
        return false;
    }

    @Override
    protected void resetExtraSettings(boolean b) {
        MavenProjectSettings projectSettings = getInitialSettings();
        if (nonRecursiveCheckBox != null) {
            nonRecursiveCheckBox.setSelected(projectSettings.isNonRecursive());
        }
        if (updateSnapshotsCheckBox != null) {
            updateSnapshotsCheckBox.setSelected(projectSettings.isUpdateSnapshots());
        }
        if (useWholeProjectContextCheckBox != null) {
            useWholeProjectContextCheckBox.setSelected(projectSettings.isUseWholeProjectContext());
        }
        if (resolveModulePerSourceSetCheckBox != null) {
            resolveModulePerSourceSetCheckBox.setSelected(projectSettings.isResolveModulePerSourceSet());
        }
        if (showPluginNodesCheckBox != null) {
            showPluginNodesCheckBox.setSelected(projectSettings.isShowPluginNodes());
        }
        if (threadCountField != null) {
            threadCountField.setText(projectSettings.getThreadCount());
        }
        if (vmOptionsField != null) {
            vmOptionsField.setText(projectSettings.getVmOptions());
        }
        if (argumentsField != null) {
            argumentsField.setText(projectSettings.getArguments());
        }
        if (argumentsImportField != null) {
            argumentsImportField.setText(projectSettings.getArgumentsImport());
        }
        if (outPutLevelCombobox != null) {
            outPutLevelCombobox.setSelectedItem(new OutputLevelComboBoxItem(projectSettings.getOutputLevel()));
        }

        if (mavenCustomPathField != null) {
            mavenCustomPathField.addBrowseFolderListener(
                    message("gmaven.settings.project.maven.custom.path.title"), null, project,
                    createSingleFolderDescriptor(), TEXT_FIELD_WHOLE_TEXT
            );
        }
        if (jdkComboBoxWrapper != null) {
            DefaultSdksModel projectSdksModel = new DefaultSdksModel();
            projectSdksModel.reset();
            recreateJdkComboBox(projectSdksModel);
            jdkComboBox.setSelectedSdk(projectSettings.getJdkName());
        }
        if (mavenHomeCombobox != null) {
            mavenHomeCombobox.removeAllItems();
            mavenHomeCombobox.addItem(
                    new DistributionSettingsComboBoxItem(DistributionSettings.getBundled())
            );
            var distributionUrl = MvnDotProperties.getDistributionUrl(project, projectSettings.getExternalProjectPath());
            if (!distributionUrl.isEmpty()) {
                mavenHomeCombobox.addItem(
                        new DistributionSettingsComboBoxItem(DistributionSettings.getWrapper(distributionUrl))
                );
            }

            DistributionSettings current = projectSettings.getDistributionSettings();
            var mavenHome = MavenUtils.resolveMavenHome();
            if (mavenHome != null) {
                mavenHomeCombobox.addItem(
                        new DistributionSettingsComboBoxItem(DistributionSettings.getLocal(mavenHome.toPath()))
                );
            }

            var customDistribution = current.getType() == CUSTOM
                    ? current : new DistributionSettings(CUSTOM, null, null);
            mavenHomeCombobox.addItem(new DistributionSettingsComboBoxItem(customDistribution));
            mavenHomeCombobox.setSelectedItem(new DistributionSettingsComboBoxItem(current));
            setupMavenHomeHintAndCustom(current);
        }
        setPreferredSize();
    }

    private void recreateJdkComboBox(@Nonnull DefaultSdksModel sdksModel) {
        if (jdkComboBox != null) {
            jdkComboBoxWrapper.remove(jdkComboBox);
        }
        jdkComboBox = new SdkComboBox(sdksModel);
        jdkComboBoxWrapper.add(jdkComboBox, BorderLayout.CENTER);
    }

    @Override
    protected void applyExtraSettings(@Nonnull MavenProjectSettings settings) {
        if (nonRecursiveCheckBox != null) {
            settings.setNonRecursive(nonRecursiveCheckBox.isSelected());
        }
        if (updateSnapshotsCheckBox != null) {
            settings.setUpdateSnapshots(updateSnapshotsCheckBox.isSelected());
        }
        if (useWholeProjectContextCheckBox != null) {
            settings.setUseWholeProjectContext(useWholeProjectContextCheckBox.isSelected());
        }
        if (resolveModulePerSourceSetCheckBox != null) {
            settings.setResolveModulePerSourceSet(resolveModulePerSourceSetCheckBox.isSelected());
        }
        if (showPluginNodesCheckBox != null) {
            settings.setShowPluginNodes(showPluginNodesCheckBox.isSelected());
        }
        if (threadCountField != null) {
            settings.setThreadCount(threadCountField.getText());
        }
        if (vmOptionsField != null) {
            settings.setVmOptions(vmOptionsField.getText());
        }
        if (argumentsField != null) {
            settings.setArguments(argumentsField.getText());
        }
        if (argumentsImportField != null) {
            settings.setArgumentsImport(argumentsImportField.getText());
        }
        if (outPutLevelCombobox != null && outPutLevelCombobox.getSelectedItem() instanceof OutputLevelComboBoxItem) {
            settings.setOutputLevel(((OutputLevelComboBoxItem) outPutLevelCombobox.getSelectedItem()).value);
        }
        if (mavenHomeCombobox != null && mavenCustomPathField != null
                && mavenHomeCombobox.getSelectedItem() instanceof DistributionSettingsComboBoxItem) {
            var distributionSettings = ((DistributionSettingsComboBoxItem) mavenHomeCombobox.getSelectedItem()).value;
            if (distributionSettings.getType() == CUSTOM) {
                distributionSettings = new DistributionSettings(CUSTOM, Path.of(mavenCustomPathField.getText()), null);
            }
            settings.setDistributionSettings(distributionSettings);
        }
        if (jdkComboBox != null && jdkComboBox.getSelectedSdkName() != null) {
            settings.setJdkName(jdkComboBox.getSelectedSdkName());
        }
    }

    @Override
    public boolean validate(@Nonnull MavenProjectSettings mavenProjectSettings) throws ConfigurationException {
        return false;
    }

    @Nonnull
    private ComboBox<OutputLevelComboBoxItem> setupOutputLevelComboBox() {
        OutputLevelComboBoxItem[] levels = Arrays.stream(OutputLevelType.values())
                .map(OutputLevelComboBoxItem::new)
                .toArray(OutputLevelComboBoxItem[]::new);
        var combobox = new ComboBox<>(levels);
        combobox.setRenderer(new MyItemCellRenderer<>());
        return combobox;
    }

    @Nonnull
    private ComboBox<DistributionSettingsComboBoxItem> setupMavenHomeComboBox() {
        DistributionSettingsComboBoxItem[] items = Stream.of(
                        DistributionSettings.getBundled()
                )
                .map(DistributionSettingsComboBoxItem::new)
                .toArray(DistributionSettingsComboBoxItem[]::new);

        var combobox = new ComboBox<>(items);
        combobox.setRenderer(new MyItemCellRenderer<>());
        combobox.addItemListener(e -> {
            DistributionSettingsComboBoxItem item = (DistributionSettingsComboBoxItem) e.getItem();
            DistributionSettings value = item.value;
            setupMavenHomeHintAndCustom(value);
        });
        combobox.setSelectedItem(null);
        return combobox;
    }

    private void setupMavenHomeHintAndCustom(DistributionSettings value) {
        if (mavenCustomPathField != null && value != null) {
            mavenCustomPathField.setVisible(value.getType() == CUSTOM);
            if (value.getType() == CUSTOM && value.getPath() != null) {
                mavenCustomPathField.setText(value.getPath().toString());
            } else {
                mavenCustomPathField.setText(null);
            }
        }
        if (wrapperHintLabel != null && value != null) {
            wrapperHintLabel.setVisible(value.getType() != CUSTOM);
            if (value.getType() == DistributionType.MVN) {
                wrapperHintLabel.setText(value.getPath().toString());
            } else {
                wrapperHintLabel.setText(value.getUrl());
            }
        }
    }

    private void setPreferredSize() {
        List<? extends JComponent> components = Arrays
                .asList(
                        vmOptionsField, argumentsField, argumentsImportField, threadCountField, mavenCustomPathField,
                        outPutLevelCombobox, mavenHomeCombobox, jdkComboBox
                );
        JComponent maxWidthComponent = components.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparing(v1 -> v1.getPreferredSize().width))
                .orElse(null);
        if (maxWidthComponent == null) return;
        components.stream()
                .filter(Objects::nonNull)
                .forEach(c -> c.setPreferredSize(maxWidthComponent.getPreferredSize()));
    }

    private final class OutputLevelComboBoxItem extends MyItem<OutputLevelType> {

        private OutputLevelComboBoxItem(@Nonnull OutputLevelType value) {
            super(Objects.requireNonNull(value));
        }

        @Nonnull
        @Override
        protected String getText() {
            return getText(value);
        }

        @Override
        protected String getComment() {
            return null;
        }

        @Nonnull
        private String getText(OutputLevelType levelType) {
            return StringUtil.capitalize(levelType.name().toLowerCase());
        }
    }

    private final class DistributionSettingsComboBoxItem extends MyItem<DistributionSettings> {

        private DistributionSettingsComboBoxItem(@Nonnull DistributionSettings value) {
            super(Objects.requireNonNull(value));
        }

        @Nonnull
        @Override
        protected String getText() {
            return getText(value);
        }

        @Override
        protected String getComment() {
            return null;
        }

        @Nonnull
        private String getText(DistributionSettings settings) {
            String text = StringUtil.capitalize(settings.getType().name().toLowerCase());
            if (settings.getType() == DistributionType.BUNDLED) {
                text += "(maven version: " + Constants.BUNDLED_MAVEN_VERSION + ")";
            }
            if (settings.getType() == DistributionType.MVN) {
                text = "Maven home(mvn)";
                try {
                    String mavenVersion = MavenUtils.getMavenVersion(settings.getPath().toFile());
                    if (mavenVersion != null) {
                        text += ": " + mavenVersion;
                    }
                } catch (Exception e) {
                    MavenLog.LOG.error(e);
                }
            }
            if (settings.getType() == DistributionType.WRAPPER) {
                text = "Use Maven wrapper";
            }
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DistributionSettingsComboBoxItem)) return false;
            DistributionSettingsComboBoxItem item = (DistributionSettingsComboBoxItem) o;
            return Objects.equals(value.getType(), item.value.getType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(value.getType());
        }
    }

    private static abstract class MyItem<T> {
        @Nonnull
        protected final T value;

        private MyItem(@Nonnull T value) {
            this.value = value;
        }

        @Nonnull
        protected abstract String getText();

        protected abstract String getComment();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MyItem item)) return false;
            return Objects.equals(value, item.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    private static class MyItemCellRenderer<T> extends ColoredListCellRenderer<MyItem<T>> {

        @Override
        protected void customizeCellRenderer(@Nonnull JList<? extends MyItem<T>> list,
                                             MyItem<T> value,
                                             int index,
                                             boolean selected,
                                             boolean hasFocus) {
            if (value == null) return;
            CompositeAppearance.DequeEnd ending = new CompositeAppearance().getEnding();
            ending.addText(value.getText(), getTextAttributes(selected));
            if (value.getComment() != null) {
                SimpleTextAttributes commentAttributes = getCommentAttributes(selected);
                ending.addComment(value.getComment(), commentAttributes);
            }
            ending.getAppearance().customize(this);
        }

        @Nonnull
        private static SimpleTextAttributes getTextAttributes(boolean selected) {
            return selected && !(SystemInfoRt.isWindows && UIManager.getLookAndFeel().getName().contains("Windows"))
                    ? SimpleTextAttributes.SELECTED_SIMPLE_CELL_ATTRIBUTES
                    : SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES;
        }

        @Nonnull
        private static SimpleTextAttributes getCommentAttributes(boolean selected) {
            return SystemInfo.isMac && selected
                    ? new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.WHITE)
                    : SimpleTextAttributes.GRAY_ATTRIBUTES;
        }
    }
}
