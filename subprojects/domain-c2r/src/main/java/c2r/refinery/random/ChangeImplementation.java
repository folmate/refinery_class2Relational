package c2r.refinery.random;

import atl.research.class_.Class;
import c2r.refinery.ClassDomain;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EContentAdapter;
import tools.refinery.store.dse.modification.DanglingEdges;
import tools.refinery.store.dse.modification.ModificationAdapter;
import tools.refinery.store.tuple.Tuple;

import static tools.refinery.store.dse.modification.DanglingEdges.DELETE;
import static tools.refinery.store.dse.modification.actions.ModificationActionLiterals.delete;


public class ChangeImplementation extends EContentAdapter {
	private final ClassDomain classmodel;
	public ChangeImplementation(ClassDomain classmodel) {
		this.classmodel = classmodel;
	}

	@Override
	public void notifyChanged(Notification notification){
		super.notifyChanged(notification);
		var feature = notification.getFeature();
		//SET,UNSET,ADD,REMOVE,ADD_MANY,REMOVE_MANY
		if(notification.getEventType()== Notification.SET
				&& feature instanceof EAttribute
				&& ((EAttribute) feature).getName().equals(	"name")){
			System.out.println("Name changed using notifier.");
			var cls = classmodel.tupleOf(notification.getNotifier());
			classmodel.name.put(cls, notification.getNewStringValue());
		}
		if(notification.getEventType()== Notification.SET
				&& feature instanceof EAttribute
				&& ((EAttribute) feature).getName().equals(	"multiValued")){
			System.out.println("MultiValued changed using notifier.");
			var cls = classmodel.tupleOf(notification.getNotifier());
			classmodel.multiValued.put(cls, (Boolean) notification.getNewValue());
		}
		if(notification.getEventType()== Notification.SET
				&& feature instanceof EReference
				&& ((EReference) feature).getName().equals(	"type")){
			System.out.println("Type change?.");
			if(notification.getNewValue()==null){
				var cls = classmodel.tupleOf(notification.getNotifier());
				var old = classmodel.tupleOf(notification.getOldValue());
				var tuple = Tuple.of(cls.get(0),old.get(0));
				classmodel.type.put(tuple,false);
			}
			//TODO new value
		}
		if(notification.getEventType()== Notification.REMOVE
				&& feature instanceof EReference
				&& ((EReference) feature).getName().equals(	"attr")){
			System.out.println("Type change?.");
			if(notification.getNewValue()==null){
				var cls = classmodel.tupleOf(notification.getNotifier());
				var old = classmodel.tupleOf(notification.getOldValue());
				var tuple = Tuple.of(cls.get(0),old.get(0));
				//classmodel.getModel().getAdapter(ModificationAdapter.class).deleteObject(
				//		old, DELETE
				//);
			}
			//TODO new value
		}


		System.out.println("New notification");
		System.out.println("\tEventType="+notification.getEventType());
		System.out.println("\tNotifier="+notification.getNotifier());
		System.out.println("\tFeature="+notification.getFeature());
		System.out.println("\tNew value="+notification.getNewValue());
		System.out.println("\tOld value="+notification.getOldValue());
	}
}
