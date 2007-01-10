/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;

public class BeanClassReferenceNode implements IReferenceNode, IRevealableReferenceNode {

    protected IJavaElement element;

    protected BeanReferenceNode parent;

    public BeanClassReferenceNode(IMember member, BeanReferenceNode parent) {
        this.element = member;
        this.parent = parent;
    }

    public IReferenceNode[] getChildren() {
        List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
        Map<IMember, MethodReference> refs = new HashMap<IMember, MethodReference>();
        for (IAopReference reference : parent.getAspectReferences()) {
            if (refs.containsKey(reference.getSource())) {
                refs.get(reference.getSource()).getAspects().add(reference);
            }
            else {
                MethodReference r = new MethodReference();
                r.setMember(reference.getSource());
                refs.put(reference.getSource(), r);
            }
        }
        for (IAopReference reference : parent.getAdviseReferences()) {
            if (refs.containsKey(reference.getTarget())) {
                refs.get(reference.getTarget()).getAdvices().add(reference);
            }
            else {
                MethodReference r = new MethodReference();
                r.setMember(reference.getTarget());
                refs.put(reference.getTarget(), r);
            }
        }
        for (Map.Entry<IMember, MethodReference> entry : refs.entrySet()) {
            nodes.add(new BeanMethodReferenceNode(entry.getKey(), entry.getValue().getAspects(),
                    entry.getValue().getAdvices()));
        }
        return nodes.toArray(new IReferenceNode[nodes.size()]);
    }

    public Image getImage() {
        return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getImage(element);
    }

    public String getText() {
        if (element instanceof IType) {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element) + " - "
                    + BeansAopUtils.getPackageLinkName(element);
        }
        else {
            return BeansAopNavigatorUtils.JAVA_LABEL_PROVIDER.getText(element);
        }
    }

    public boolean hasChildren() {
        return parent.getAdviseReferences().size() > 0 || parent.getAspectReferences().size() > 0;
    }

    public void openAndReveal() {
        IEditorPart p;
        try {
            p = JavaUI.openInEditor(element);
            JavaUI.revealInEditor(p, element);
        }
        catch (Exception e) {
        }
    }

    public int getLineNumber() {
        return BeansAopNavigatorUtils.getLineNumber((IMember) element);
    }

    public IResource getResource() {
        return element.getResource();
    }

    class MethodReference {

        List<IAopReference> aspects = new ArrayList<IAopReference>();

        List<IAopReference> advices = new ArrayList<IAopReference>();

        IMember member;

        public List<IAopReference> getAdvices() {
            return advices;
        }

        public void setAdvices(List<IAopReference> advices) {
            this.advices = advices;
        }

        public List<IAopReference> getAspects() {
            return aspects;
        }

        public void setAspects(List<IAopReference> aspects) {
            this.aspects = aspects;
        }

        public IMember getMember() {
            return member;
        }

        public void setMember(IMember member) {
            this.member = member;
        }
    }

}
